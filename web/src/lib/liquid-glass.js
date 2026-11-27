/*!
 * Liquid Glass for the web — v2.0.0
 * Apple-style Liquid Glass in pure HTML, CSS and JavaScript. No framework, no build step.
 *
 * https://github.com/asimov-academy/liquid-glass-skill
 * MIT License — Asimov Academy
 *
 * The material model (frost/fill backdrop, tone curves, edge refraction
 * profile and rim lighting) is a vanilla-JS port of Glass-HQ/liquid-glass
 * (https://github.com/Glass-HQ/liquid-glass), MIT License, © 2026 Glass HQ.
 */
(function (root, factory) {
  var api = factory(root);
  if (typeof module === 'object' && module.exports) module.exports = api;
  if (root) root.LiquidGlass = api;
})(typeof window !== 'undefined' ? window : this, function (win) {
  'use strict';

  if (!win || !win.document) return {};
  var doc = win.document;
  var SVG_NS = 'http://www.w3.org/2000/svg';
  var VERSION = '2.0.0';

  /* ------------------------------------------------------------------ *
   * Environment
   * ------------------------------------------------------------------ */

  var ua = win.navigator.userAgent || '';
  var brands = (win.navigator.userAgentData && win.navigator.userAgentData.brands) || [];
  // Only Chromium can feed an SVG filter (url()) into `backdrop-filter`.
  var isChromium = brands.some(function (b) { return /Chromium|Google Chrome|Microsoft Edge|Opera/i.test(b.brand); }) ||
    (/Chrome\/\d+/.test(ua) && !/iPhone|iPad|iPod/.test(ua));

  function media(q) {
    try { return win.matchMedia(q); } catch (e) { return { matches: false, addEventListener: function () {} }; }
  }
  var mqReduceMotion = media('(prefers-reduced-motion: reduce)');
  var mqReduceTransparency = media('(prefers-reduced-transparency: reduce)');
  var mqMoreContrast = media('(prefers-contrast: more)');
  var mqDark = media('(prefers-color-scheme: dark)');

  var supports = {
    backdropRefraction: isChromium,
    backdropFilter: !!(win.CSS && (CSS.supports('backdrop-filter', 'blur(1px)') || CSS.supports('-webkit-backdrop-filter', 'blur(1px)'))),
    svgFilters: true
  };

  /* ------------------------------------------------------------------ *
   * Materials (calibrated values from Glass-HQ/liquid-glass, MIT)
   * ------------------------------------------------------------------ */

  var MATERIALS = {
    clear: {
      blur: 0,
      refraction: 60,
      // selective shadow lift / highlight rolloff; middle tones preserved
      light: { shadowLift: 0.18, highlightRolloff: 0 },
      dark: { shadowLift: 0.15, highlightRolloff: 0.2 }
    },
    regular: {
      blur: 7.33,
      refraction: 60,
      light: { fillSigma: 23, fillOpacity: 0.34, tone: [0.36, 0.67, -0.1113, 1.1031] },
      dark: { fillSigma: 21.8, fillOpacity: 0.65, tone: [0.105, 0.83, -0.2091, 1.0648] }
    }
  };

  var defaults = {
    variant: 'regular',   // 'regular' | 'clear'
    refraction: null,     // px the rim pulls the backdrop inward (null = auto from size, max 60)
    bezel: 20,            // depth of the curved rim, px
    magnify: 1,           // lens zoom of the flat center (1 = none)
    dispersion: 0,        // chromatic aberration (0 – 0.5)
    tint: null,           // '#rrggbb' — colored glass
    interactive: false,   // spring press / stretch / glow
    mode: 'auto'          // 'auto' | 'backdrop' | 'scene' | 'fallback'
  };

  /* ------------------------------------------------------------------ *
   * Utils
   * ------------------------------------------------------------------ */

  var uid = 0;
  function clamp(v, a, b) { return v < a ? a : v > b ? b : v; }
  function lerp(a, b, t) { return a + (b - a) * t; }
  function round(v, p) { var k = Math.pow(10, p == null ? 2 : p); return Math.round(v * k) / k; }
  function el(tag, cls, parent) {
    var n = doc.createElement(tag);
    if (cls) n.className = cls;
    if (parent) parent.appendChild(n);
    return n;
  }
  function parseVal(v) {
    if (v === '' || v === 'true') return true;
    if (v === 'false') return false;
    return /^\s*-?\d*\.?\d+(px)?\s*$/.test(v) ? parseFloat(v) : v;
  }
  function readAttrOptions(node) {
    var out = {};
    var ds = node.dataset || {};
    if (ds.glass && ds.glass !== 'true' && MATERIALS[ds.glass]) out.variant = ds.glass;
    for (var k in ds) {
      if (k.indexOf('glass') === 0 && k.length > 5) {
        var key = k.charAt(5).toLowerCase() + k.slice(6);
        if (key === 'appearance' || key === 'scene' || key === 'backdrop' || key === 'group') continue;
        out[key] = parseVal(ds[k]);
      }
    }
    return out;
  }
  function assign(t) {
    for (var i = 1; i < arguments.length; i++) {
      var s = arguments[i];
      if (s) for (var k in s) if (s[k] !== undefined) t[k] = s[k];
    }
    return t;
  }
  function resolveOptions(node, opts) {
    var o = assign({}, defaults, readAttrOptions(node), opts);
    if (!MATERIALS[o.variant]) o.variant = 'regular';
    o.refraction = o.refraction == null || o.refraction === '' || isNaN(+o.refraction) ? null : +o.refraction;
    o.bezel = clamp(+o.bezel || 20, 2, 200);
    o.magnify = +o.magnify || 1;
    o.dispersion = clamp(+o.dispersion || 0, 0, 0.8);
    o.tint = typeof o.tint === 'string' && /^#[\da-f]{6}$/i.test(o.tint) ? o.tint : null;
    return o;
  }
  // Big surfaces bend the full 60px; small controls bend proportionally less,
  // so the opposite rims never sample across each other.
  function autoRefraction(o, w, h) {
    if (o.refraction != null) return o.refraction;
    var md = Math.min(w, h);
    return clamp(md * 0.2 + Math.max(0, md - 60) * 0.9, 1.5, MATERIALS[o.variant].refraction);
  }
  function appearanceOf(node) {
    var host = node.closest ? node.closest('[data-glass-appearance]') : null;
    var a = host ? host.getAttribute('data-glass-appearance') : 'light';
    if (a === 'auto') a = mqDark.matches ? 'dark' : 'light';
    return a === 'dark' ? 'dark' : 'light';
  }
  function radiusOf(node, w, h) {
    var cs = win.getComputedStyle(node);
    var r = cs.borderTopLeftRadius || '0';
    var v = parseFloat(r) || 0;
    if (/%/.test(r)) v = Math.min(w, h) * v / 100;
    return clamp(v, 0, Math.min(w, h) / 2);
  }
  function tintChannels(hex) {
    return hex ? [1, 3, 5].map(function (i) { return parseInt(hex.slice(i, i + 2), 16) / 255; }) : null;
  }

  /* ------------------------------------------------------------------ *
   * Frame loop + springs
   * ------------------------------------------------------------------ */

  var tasks = new Set();
  var running = false, lastT = 0;
  function frame(t) {
    var dt = Math.min(0.05, Math.max(0.001, (t - lastT) / 1000 || 0.016));
    lastT = t;
    tasks.forEach(function (fn) { if (!fn(dt)) tasks.delete(fn); });
    if (tasks.size) win.requestAnimationFrame(frame); else running = false;
  }
  function animate(fn) {
    tasks.add(fn);
    if (!running) { running = true; lastT = win.performance.now(); win.requestAnimationFrame(frame); }
  }

  /**
   * Damped spring (semi-implicit Euler, 240 Hz substeps). Springs keep their
   * velocity when retargeted, so interrupted gestures stay continuous.
   */
  function Spring(value, cfg) {
    cfg = cfg || {};
    this.value = value || 0; this.target = this.value; this.velocity = 0;
    this.stiffness = cfg.stiffness || 300; this.damping = cfg.damping || 26; this.mass = cfg.mass || 1;
    this.precision = cfg.precision || 0.001;
  }
  Spring.prototype.to = function (t) { this.target = t; if (mqReduceMotion.matches) this.set(t); return this; };
  Spring.prototype.set = function (v) { this.value = this.target = v; this.velocity = 0; return this; };
  Spring.prototype.step = function (dt) {
    var n = Math.ceil(dt * 240), h = dt / n;
    for (var i = 0; i < n; i++) {
      var f = -this.stiffness * (this.value - this.target) - this.damping * this.velocity;
      this.velocity += (f / this.mass) * h;
      this.value += this.velocity * h;
    }
    if (Math.abs(this.velocity) < this.precision && Math.abs(this.value - this.target) < this.precision) {
      this.value = this.target; this.velocity = 0; return false;
    }
    return true;
  };

  /* ------------------------------------------------------------------ *
   * Maps — the optics
   *
   * For every pixel we take the signed distance d to the outline (negative
   * inside) and the outward normal n. The rim is a circular bevel `bezel`
   * px deep: edge(d) = 1 − √((2 − t)·t), t = clamp(−d / bezel). The field
   * map stores −n·edge·refraction in R/G, so near the rim each pixel shows
   * backdrop pulled from further inside — content stretches along the curve,
   * exactly how thick glass bends light. Beyond the bevel the vector is 0.
   *
   * The overlay map is the rim lighting: a 0.9 px specular stroke on the
   * top and bottom of the outline with a soft 4.5 px diffuse glow, and a
   * thin dark line hugging the sides.
   * ------------------------------------------------------------------ */

  var COS_SPREAD = Math.cos(1.08), COS_DIFFUSE = Math.cos(1.08 * 0.65), COS_SIDE = Math.cos(0.98);
  var MARGIN = 2; // maps cover the shape plus 2 px on every side

  function edgeOf(d, bezel) {
    var t = clamp(-d / bezel, 0, 1);
    return 1 - clamp(Math.sqrt((2 - t) * t), 0, 1);
  }
  // rim lighting for one sample → [light (0–1), alpha (0–1)]
  function rimLight(d, nx, ny, aa, dark) {
    var cov = clamp(0.5 - d / aa, 0, 1);
    var ay = Math.abs(ny);
    var directional = Math.pow(clamp((ay - COS_SPREAD) / (1 - COS_SPREAD), 0, 1), 1.25);
    var diffuseDir = Math.pow(clamp((ay - COS_DIFFUSE) / (1 - COS_DIFFUSE), 0, 1), 1.25);
    var stroke = clamp((d + 0.9) / aa + 0.5, 0, 1) * cov;
    var spec = directional * stroke;
    spec = spec / (1 + 0.5 * (1 - spec));
    var fall4 = 1 - clamp(-d / 4.5, 0, 1);
    var diffuse = diffuseDir * 0.18 * fall4 * fall4 * cov;
    var hl = clamp(spec * (dark ? 0.82 : 1) + diffuse * (dark ? 0.4 : 1), 0, 1);
    var adj = d - 0.5, fall = 1 - clamp(-adj / 0.5, 0, 1);
    var ramp = lerp(fall > 0 ? 1 : 0, fall, 0.75);
    var outline = ramp * clamp((adj + 0.5) / aa + 0.5, 0, 1) * clamp(-adj / aa + 0.5, 0, 1);
    var w1 = clamp((nx - COS_SIDE) / (1 - COS_SIDE), 0, 1) * outline;
    var w2 = clamp((-nx - COS_SIDE) / (1 - COS_SIDE), 0, 1) * outline;
    var shade = (w1 / (1 + 0.4 * (1 - w1)) + w2 / (1 + 0.4 * (1 - w2))) * (dark ? 0.6 : 0.48);
    var alpha = hl + shade * (1 - hl);
    return [alpha > 1e-5 ? (dark ? 0.82 : 1) * hl / alpha : 0, alpha];
  }

  function mapScale(w, h) {
    var dpr = clamp(win.devicePixelRatio || 1, 1, 2);
    var fit = Math.min(1, 4096 / ((w + 2 * MARGIN) * dpr), 4096 / ((h + 2 * MARGIN) * dpr));
    return Math.max(0.25, dpr * fit);
  }

  // Shared writer: `sample(px, py)` returns [d, nx, ny] in CSS px, relative to
  // the map's top-left (which sits MARGIN px outside the shape box).
  function renderMaps(W, H, s, sample, o) {
    var zoomK = 1 / o.magnify - 1;
    var maxD = Math.max(1, o.refraction + Math.abs(zoomK) * Math.max(o.hw || 0, o.hh || 0));
    var mk = function () { var c = doc.createElement('canvas'); c.width = W; c.height = H; return c; };
    var cf = mk(), co = mk(), cm = mk();
    var xf = cf.getContext('2d'), xo = co.getContext('2d'), xm = cm.getContext('2d');
    var F = xf.createImageData(W, H), O = xo.createImageData(W, H), M = xm.createImageData(W, H);
    var fd = F.data, od = O.data, md = M.data, aa = 1 / s, k = 0.5 / maxD;
    for (var j = 0; j < H; j++) {
      var y = (j + 0.5) / s;
      for (var i = 0; i < W; i++) {
        var x = (i + 0.5) / s, q = sample(x, y), d = q[0], nx = q[1], ny = q[2];
        var e = edgeOf(Math.min(d, 0), o.bezel) * o.refraction * (q[3] == null ? 1 : q[3]);
        var vx = -nx * e, vy = -ny * e;
        if (zoomK) { vx += (x - o.cx) * zoomK; vy += (y - o.cy) * zoomK; }
        var p = (j * W + i) * 4;
        fd[p] = clamp(Math.round(255 * (0.5 + vx * k)), 0, 255);
        fd[p + 1] = clamp(Math.round(255 * (0.5 + vy * k)), 0, 255);
        fd[p + 2] = 128; fd[p + 3] = 255; // opaque: premultiplied alpha would corrupt vectors
        var cov = clamp(0.5 - d / aa, 0, 1);
        md[p] = md[p + 1] = md[p + 2] = 255; md[p + 3] = Math.round(cov * 255);
        if (d > -6) {
          var L = rimLight(d, nx, ny, aa, o.dark), v = Math.round(L[0] * 255);
          od[p] = od[p + 1] = od[p + 2] = v; od[p + 3] = Math.round(L[1] * 255);
        }
      }
    }
    xf.putImageData(F, 0, 0); xo.putImageData(O, 0, 0); xm.putImageData(M, 0, 0);
    return { field: cf.toDataURL('image/png'), overlay: co.toDataURL('image/png'), mask: cm.toDataURL('image/png'), scale: 2 * maxD };
  }

  var mapCache = new Map();
  function makeMaps(w, h, r, o) {
    var s = mapScale(w, h);
    var key = [round(w, 1), round(h, 1), round(r, 1), round(o.refraction, 2), o.bezel, o.magnify, o.dark ? 1 : 0, s].join('|');
    var hit = mapCache.get(key);
    if (hit) { mapCache.delete(key); mapCache.set(key, hit); return hit; }
    var W = Math.max(2, Math.round((w + 2 * MARGIN) * s)), H = Math.max(2, Math.round((h + 2 * MARGIN) * s));
    var hw = w / 2, hh = h / 2;
    r = Math.min(r, hw, hh);
    var ex = hw - r, ey = hh - r, ox = MARGIN + hw, oy = MARGIN + hh;
    var sample = function (x, y) {
      var px = x - ox, py = y - oy, ax = Math.abs(px), ay = Math.abs(py);
      var qx = ax - ex, qy = ay - ey, sx = px < 0 ? -1 : 1, sy = py < 0 ? -1 : 1;
      if (qx > 0 && qy > 0) {
        var l = Math.sqrt(qx * qx + qy * qy) || 1;
        return [l - r, qx / l * sx, qy / l * sy];
      }
      return qx > qy ? [qx - r, sx, 0] : [qy - r, 0, sy];
    };
    var out = renderMaps(W, H, s, sample, assign({}, o, { hw: hw, hh: hh, cx: ox, cy: oy }));
    mapCache.set(key, out);
    if (mapCache.size > 80) mapCache.delete(mapCache.keys().next().value);
    return out;
  }

  /* ------------------------------------------------------------------ *
   * SVG host + material filter graph
   * ------------------------------------------------------------------ */

  var svgHost = null;
  function defs() {
    if (svgHost && svgHost.isConnected) return svgHost.firstChild;
    svgHost = doc.createElementNS(SVG_NS, 'svg');
    svgHost.setAttribute('aria-hidden', 'true');
    svgHost.setAttribute('width', '0'); svgHost.setAttribute('height', '0');
    svgHost.setAttribute('data-liquid-glass', VERSION);
    svgHost.style.cssText = 'position:absolute;width:0;height:0;overflow:hidden;pointer-events:none;left:-9999px;top:-9999px';
    svgHost.appendChild(doc.createElementNS(SVG_NS, 'defs'));
    (doc.body || doc.documentElement).appendChild(svgHost);
    return svgHost.firstChild;
  }
  function makeFilter(id, attrs, inner) {
    var f = doc.createElementNS(SVG_NS, 'filter');
    f.setAttribute('id', id);
    for (var k in attrs) f.setAttribute(k, attrs[k]);
    f.innerHTML = inner;
    defs().appendChild(f);
    return f;
  }
  function rect(x, y, w, h) { return ' x="' + round(x, 3) + '" y="' + round(y, 3) + '" width="' + round(Math.max(0, w), 3) + '" height="' + round(Math.max(0, h), 3) + '"'; }
  function img(href, res, attrs) {
    return '<feImage href="' + href + '" xlink:href="' + href + '" preserveAspectRatio="none" result="' + res + '"' + attrs + '/>';
  }
  function table(fn) {
    var a = [];
    for (var n = 0; n <= 32; n++) a.push(round(fn(n / 32), 4));
    return a.join(' ');
  }
  function toneTable(o, dark) {
    if (o.variant === 'regular') {
      var spec = dark ? MATERIALS.regular.dark : MATERIALS.regular.light, t = spec.tone, wgt = dark ? 0.9 : 0.8;
      return table(function (v) { return wgt * (t[0] + t[1] * v + t[2] * v * v) + (1 - wgt) * v; });
    }
    var c = dark ? MATERIALS.clear.dark : MATERIALS.clear.light;
    return table(function (v) {
      var tone = v + c.shadowLift * Math.pow(1 - v, 3) - c.highlightRolloff * Math.pow(v, 3);
      var tinted = dark ? 0.38 * v + 0.2 : 0.78 * v + 0.26, tw = dark ? 0.14 : 0.06;
      return clamp((1 - tw) * tone + tw * tinted, 0, 1);
    });
  }

  /**
   * The material graph for one lens, as filter primitives.
   *   P(role) returns the extra attributes of a primitive: its subregion
   *   (and, in scene mode, a marker the scene uses to move it).
   *   u = { blur(sigma) → stdDeviation string, scale(px) → scale string }
   * Reads `src`, writes `${p}color`.
   */
  function materialGraph(p, src, mapHref, scale, o, dark, P, u, axis) {
    var s = '', input = src;
    if (o.variant === 'regular') {
      var spec = dark ? MATERIALS.regular.dark : MATERIALS.regular.light;
      s += '<feGaussianBlur in="' + src + '" stdDeviation="' + u.blur(MATERIALS.regular.blur) + '" data-sigma="' + MATERIALS.regular.blur + '" result="' + p + 'frost"' + P('pad') + '/>' +
        '<feGaussianBlur in="' + src + '" stdDeviation="' + u.blur(spec.fillSigma) + '" data-sigma="' + spec.fillSigma + '" result="' + p + 'fill"' + P('pad') + '/>' +
        '<feBlend in="' + p + 'frost" in2="' + p + 'fill" mode="' + (dark ? 'darken' : 'lighten') + '" result="' + p + 'blend"' + P('pad') + '/>' +
        '<feComposite in="' + p + 'blend" in2="' + p + 'frost" operator="arithmetic" k1="0" k2="' + spec.fillOpacity + '" k3="' + round(1 - spec.fillOpacity, 4) + '" k4="0" result="' + p + 'mix"' + P('pad') + '/>';
      input = p + 'mix';
    }
    s += img(mapHref, p + (axis ? 'map0' : 'map'), P('map'));
    if (axis) s += '<feColorMatrix in="' + p + 'map0" type="matrix" values="1 0 0 0 0 0 1 0 0 0 0 0 1 0 0 0 0 0 1 0" data-axis="1" result="' + p + 'map"' + P('map') + '/>';
    var DM = function (k, out) {
      return '<feDisplacementMap in="' + input + '" in2="' + p + 'map" scale="' + u.scale(scale * k) + '" data-scale="' + round(scale * k, 3) + '" xChannelSelector="R" yChannelSelector="G" result="' + out + '"' + P('map') + '/>';
    };
    if (!o.dispersion) s += DM(1, p + 'refracted');
    else {
      var D = o.dispersion, ch = ['1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0', '0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 1 0', '0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 1 0'];
      s += DM(1 + D, p + 'dr') + '<feColorMatrix in="' + p + 'dr" type="matrix" values="' + ch[0] + '" result="' + p + 'cr"' + P('map') + '/>' +
        DM(1, p + 'dg') + '<feColorMatrix in="' + p + 'dg" type="matrix" values="' + ch[1] + '" result="' + p + 'cg"' + P('map') + '/>' +
        DM(1 - D, p + 'db') + '<feColorMatrix in="' + p + 'db" type="matrix" values="' + ch[2] + '" result="' + p + 'cb"' + P('map') + '/>' +
        '<feBlend in="' + p + 'cr" in2="' + p + 'cg" mode="screen" result="' + p + 'rg"' + P('map') + '/>' +
        '<feBlend in="' + p + 'rg" in2="' + p + 'cb" mode="screen" result="' + p + 'refracted"' + P('map') + '/>';
    }
    var tv = toneTable(o, dark);
    if (o.variant === 'regular') {
      var chroma = (dark ? MATERIALS.regular.dark : MATERIALS.regular.light).tone[3];
      s += '<feComponentTransfer in="' + p + 'refracted" result="' + p + 'tone"' + P('map') + '>' +
        '<feFuncR type="table" tableValues="' + tv + '"/><feFuncG type="table" tableValues="' + tv + '"/><feFuncB type="table" tableValues="' + tv + '"/></feComponentTransfer>' +
        '<feColorMatrix in="' + p + 'tone" type="saturate" values="' + chroma + '" result="' + p + 'color"' + P('map') + '/>';
    } else {
      s += '<feComponentTransfer in="' + p + 'refracted" result="' + p + 'color"' + P('map') + '>' +
        '<feFuncR type="table" tableValues="' + tv + '"/><feFuncG type="table" tableValues="' + tv + '"/><feFuncB type="table" tableValues="' + tv + '"/></feComponentTransfer>';
    }
    var tint = tintChannels(o.tint);
    if (tint) {
      // colored glass transmits luminance instead of mixing in neutral RGB
      var body = o.variant === 'regular' ? 0.78 : 0.68;
      s += '<feColorMatrix in="' + p + 'color" type="saturate" values="0" result="' + p + 'lum"' + P('map') + '/>' +
        '<feComponentTransfer in="' + p + 'lum" result="' + p + 'tinted"' + P('map') + '>' +
        ['R', 'G', 'B'].map(function (c, i) { return '<feFunc' + c + ' type="linear" slope="' + round(tint[i] * (1 - body), 4) + '" intercept="' + round(tint[i] * body, 4) + '"/>'; }).join('') +
        '<feFuncA type="linear" slope="' + round(1 - body, 4) + '" intercept="' + body + '"/></feComponentTransfer>';
      return { markup: s, out: p + 'tinted' };
    }
    return { markup: s, out: p + 'color' };
  }

  /* ------------------------------------------------------------------ *
   * Scene — cross-browser refraction (Safari, Firefox, or forced). One SVG
   * filter on the `.lg-backdrop` renders every lens of the scene.
   *
   * All coordinates are fractions of the backdrop box
   * (primitiveUnits="objectBoundingBox"): WebKit resolves userSpaceOnUse for
   * CSS filters on HTML against the page, not the element, so fractions are
   * the only units every engine agrees on. In those units displacement scales
   * X by the box width and Y by its height, so each map goes through a colour
   * matrix that rebalances the axes.
   * ------------------------------------------------------------------ */

  var scenes = new Map();
  function findBackdrop(sceneEl) {
    var list = sceneEl.querySelectorAll('.lg-backdrop, [data-glass-backdrop]');
    for (var i = 0; i < list.length; i++) {
      if (list[i].parentElement && list[i].parentElement.closest('.lg-scene, [data-glass-scene]') === sceneEl) return list[i];
    }
    return null;
  }
  function sceneFor(sceneEl) {
    var s = scenes.get(sceneEl);
    if (!s) {
      var bd = findBackdrop(sceneEl);
      if (!bd) return null;
      s = new Scene(sceneEl, bd); scenes.set(sceneEl, s);
    }
    return s;
  }

  function Scene(sceneEl, backdrop) {
    this.el = sceneEl; this.backdrop = backdrop; this.id = 'lg-scene-' + (++uid);
    this.lenses = new Set(); this.filter = null; this.visible = true; this.dirty = true;
    var self = this;
    if ('IntersectionObserver' in win) {
      this.io = new IntersectionObserver(function (es) {
        self.visible = es[es.length - 1].isIntersecting;
        if (self.visible) self.wake();
      }, { rootMargin: '200px' });
      this.io.observe(sceneEl);
    }
    this.tick = function () {
      if (!self.lenses.size) return false;
      if (self.dirty) self.rebuild();
      if (self.visible) self.update();
      return self.visible && !!self.lenses.size;
    };
  }
  Scene.prototype.wake = function () { animate(this.tick); };
  Scene.prototype.add = function (g) { this.lenses.add(g); this.dirty = true; this.wake(); };
  Scene.prototype.remove = function (g) {
    this.lenses.delete(g); this.dirty = true;
    if (!this.lenses.size) {
      if (this.filter) this.filter.remove(); this.filter = null;
      this.backdrop.style.filter = '';
      if (this.io) this.io.disconnect();
      scenes.delete(this.el);
    } else this.wake();
  };
  Scene.prototype.rebuild = function () {
    this.dirty = false;
    var html = '', i = 0, self = this;
    this.items = [];
    var u = { blur: function () { return '0'; }, scale: function () { return '0'; } };
    this.lenses.forEach(function (g) {
      if (!g.maps) return;
      var n = 'l' + (i++);
      var P = function (role) { return ' data-r="' + n + '" data-k="' + role + '"'; };
      var gph = materialGraph(n, 'SourceGraphic', g.maps.field, g.maps.scale, g.opts, g.dark, P, u, true);
      html += gph.markup + img(g.maps.mask, n + 'k', P('map')) +
        '<feComposite in="' + gph.out + '" in2="' + n + 'k" operator="in" result="' + n + 'i"' + P('map') + '/>' +
        // the lens follows the element's opacity (fades, hidden popovers)
        '<feComponentTransfer in="' + n + 'i" result="' + n + 'o"' + P('map') + '><feFuncA type="linear" slope="1" data-op="1"/></feComponentTransfer>';
      self.items.push({ g: g, n: n, last: '' });
    });
    html += '<feMerge><feMergeNode in="SourceGraphic"/>' + this.items.map(function (it) { return '<feMergeNode in="' + it.n + 'o" data-m="' + it.n + '"/>'; }).join('') + '</feMerge>';
    if (this.filter) this.filter.remove();
    this.filter = makeFilter(this.id + '-' + uid++, {
      x: 0, y: 0, width: 1, height: 1,
      filterUnits: 'objectBoundingBox', primitiveUnits: 'objectBoundingBox',
      'color-interpolation-filters': 'sRGB'
    }, html);
    this.items.forEach(function (it) {
      it.nodes = Array.prototype.slice.call(self.filter.querySelectorAll('[data-r="' + it.n + '"]'));
      it.merge = self.filter.querySelector('[data-m="' + it.n + '"]');
      it.op = self.filter.querySelector('[data-r="' + it.n + '"] [data-op]');
    });
    this.backdrop.style.filter = 'url(#' + this.filter.id + ')';
  };
  Scene.prototype.update = function () {
    var bd = this.backdrop, br = bd.getBoundingClientRect();
    var bw = bd.offsetWidth || br.width, bh = bd.offsetHeight || br.height;
    if (!bw || !bh) return;
    var kx = br.width ? bw / br.width : 1, ky = br.height ? bh / br.height : 1;
    var mn = Math.min(bw, bh), ax = mn / bw, ay = mn / bh;
    var axis = round(ax, 6) + ' 0 0 0 ' + round((1 - ax) / 2, 6) + ' 0 ' + round(ay, 6) + ' 0 0 ' + round((1 - ay) / 2, 6) + ' 0 0 1 0 0 0 0 0 1 0';
    var sceneEl = this.el;
    var reads = this.items.map(function (it) {
      var g = it.g, r = g.el.getBoundingClientRect();
      var op = 1;
      for (var a = g.el; a && a !== sceneEl; a = a.parentElement) {
        var cs = win.getComputedStyle(a);
        op *= +cs.opacity;
        if (cs.visibility === 'hidden') op = 0;
      }
      var x = (r.left - br.left) * kx, y = (r.top - br.top) * ky, w = r.width * kx, h = r.height * ky;
      // A lens that is switched off, invisible or outside the backdrop keeps
      // valid geometry (WebKit drops the whole filter if a primitive leaves
      // the region) and just leaves the merge.
      var outside = x + w <= 0 || y + h <= 0 || x >= bw || y >= bh;
      var off = g.el.getAttribute('data-lg-lens') === 'off' || !r.width || op < 0.01 || outside;
      if (!r.width || outside) { x = 0; y = 0; w = Math.max(1, Math.min(w, bw)); h = Math.max(1, Math.min(h, bh)); }
      return { it: it, off: off, op: op, x: x, y: y, w: w, h: h, lw: g.el.offsetWidth || 1 };
    });
    reads.forEach(function (m) {
      var it = m.it, o = it.g.opts;
      var sig = [m.off ? 0 : 1, round(m.x, 1), round(m.y, 1), round(m.w, 1), round(m.h, 1), bw, bh, round(m.op, 3)].join(',');
      if (sig === it.last) return;
      it.last = sig;
      if (it.merge) it.merge.setAttribute('in', m.off ? 'SourceGraphic' : it.n + 'o');
      if (it.op) it.op.setAttribute('slope', round(clamp(m.op, 0, 1), 3));
      if (m.off && it.placed) return;
      it.placed = true;
      var k = m.lw ? m.w / m.lw : 1, M = MARGIN * k;
      var pad = 3 * 23 + (it.g.refraction || 0) * k + 4;
      it.nodes.forEach(function (n) {
        var role = n.getAttribute('data-k');
        var x = m.x - M, y = m.y - M, w = m.w + 2 * M, h = m.h + 2 * M;
        if (role === 'pad') { x -= pad; y -= pad; w += 2 * pad; h += 2 * pad; }
        n.setAttribute('x', round(x / bw, 6)); n.setAttribute('y', round(y / bh, 6));
        n.setAttribute('width', round(Math.max(0, w) / bw, 6)); n.setAttribute('height', round(Math.max(0, h) / bh, 6));
        if (n.hasAttribute('data-scale')) n.setAttribute('scale', round(parseFloat(n.getAttribute('data-scale')) * k / mn, 6));
        if (n.hasAttribute('data-axis')) n.setAttribute('values', axis);
        if (n.hasAttribute('data-sigma')) {
          var b = parseFloat(n.getAttribute('data-sigma')) * k;
          n.setAttribute('stdDeviation', round(b / bw, 6) + ' ' + round(b / bh, 6));
        }
      });
    });
  };

  /* ------------------------------------------------------------------ *
   * Glass element
   * ------------------------------------------------------------------ */

  var instances = new WeakMap();
  var GROUP_SELECTOR = '.lg-glass-group, [data-glass-group]';

  function Glass(node, opts) {
    this.el = node; this.id = ++uid; this.userOpts = opts || {};
    this.opts = resolveOptions(node, this.userOpts);
    this.filter = null; this.scene = null; this.maps = null;
    instances.set(node, this);

    node.classList.add('lg-ready');
    if (win.getComputedStyle(node).position === 'static') node.style.position = 'relative';

    var layers = el('span', 'lg-layers');
    layers.setAttribute('aria-hidden', 'true');
    this.refract = el('span', 'lg-refract', layers);
    this.fill = el('span', 'lg-fill', layers);
    this.glow = el('span', 'lg-glow', layers);
    this.overlay = el('span', 'lg-overlay', layers);
    this.layers = layers;
    node.insertBefore(layers, node.firstChild);

    var self = this;
    this.queued = false;
    this.ro = new ResizeObserver(function () { self.schedule(); });
    this.ro.observe(node);
    if (this.opts.interactive) this.bindInteractive();
    this.render();
  }

  Glass.prototype.decideMode = function () {
    var m = this.opts.mode;
    if (this.el.parentElement && this.el.parentElement.closest(GROUP_SELECTOR)) return 'member';
    if (mqReduceTransparency.matches || mqMoreContrast.matches) return 'fallback';
    var sceneEl = this.el.parentElement && this.el.parentElement.closest('.lg-scene, [data-glass-scene]');
    var canScene = !!(sceneEl && findBackdrop(sceneEl));
    if (m === 'fallback') return 'fallback';
    if (m === 'backdrop') return supports.backdropRefraction ? 'backdrop' : (canScene ? 'scene' : 'fallback');
    if (m === 'scene') return canScene ? 'scene' : (supports.backdropRefraction ? 'backdrop' : 'fallback');
    if (supports.backdropRefraction) return 'backdrop';
    return canScene ? 'scene' : 'fallback';
  };

  Glass.prototype.schedule = function () {
    if (this.queued) return;
    this.queued = true;
    var self = this;
    win.requestAnimationFrame(function () { self.queued = false; self.render(); });
  };

  Glass.prototype.update = function (opts) {
    assign(this.userOpts, opts);
    var wasInteractive = this.opts.interactive;
    this.opts = resolveOptions(this.el, this.userOpts);
    if (this.opts.interactive && !wasInteractive) this.bindInteractive();
    this.lastSig = '';
    this.render();
    return this;
  };

  Glass.prototype.render = function () {
    var node = this.el;
    if (!node.isConnected) return;
    var w = node.offsetWidth, h = node.offsetHeight;
    if (!w || !h) return;
    var o = this.opts, r = radiusOf(node, w, h);
    var mode = this.decideMode(), dark = appearanceOf(node) === 'dark';
    var sig = [mode, w, h, r, dark, o.variant, o.refraction, o.bezel, o.magnify, o.dispersion, o.tint].join('|');
    if (sig === this.lastSig) return;
    this.lastSig = sig;
    this.mode = mode; this.dark = dark;
    node.setAttribute('data-lg-mode', mode);
    node.setAttribute('data-lg-variant', o.variant);
    node.setAttribute('data-lg-appearance', dark ? 'dark' : 'light');
    node.toggleAttribute('data-lg-tinted', !!o.tint);
    if (o.tint) node.style.setProperty('--lg-tint', o.tint); else node.style.removeProperty('--lg-tint');
    node.style.setProperty('--lg-r', round(r) + 'px');

    var rs = this.refract.style;
    if (mode !== 'scene' && this.scene) { this.scene.remove(this); this.scene = null; }
    if (mode !== 'backdrop' && this.filter) { this.filter.remove(); this.filter = null; }

    if (mode === 'member') {
      rs.backdropFilter = rs.webkitBackdropFilter = '';
      this.overlay.style.backgroundImage = '';
      wakeGroupOf(node);
      return;
    }

    this.refraction = autoRefraction(o, w, h);
    this.maps = makeMaps(w, h, r, { refraction: this.refraction, bezel: o.bezel, magnify: o.magnify, dark: dark });
    this.overlay.style.backgroundImage = 'url("' + this.maps.overlay + '")';

    if (mode === 'fallback') {
      var fb = o.variant === 'regular' ? 'blur(18px) saturate(1.4)' : 'blur(1px) saturate(1.1)';
      rs.backdropFilter = rs.webkitBackdropFilter = fb;
      return;
    }

    if (mode === 'scene') {
      rs.backdropFilter = rs.webkitBackdropFilter = '';
      var s = sceneFor(node.parentElement.closest('.lg-scene, [data-glass-scene]'));
      if (this.scene && this.scene !== s) this.scene.remove(this);
      this.scene = s;
      s.add(this); s.dirty = true; s.wake();
      return;
    }

    // backdrop mode (Chromium): the whole material graph runs as the
    // backdrop-filter. The refract layer keeps the element's own
    // border-radius — the one clip Chromium always honours for backdrops.
    var fid = 'lg-glass-' + this.id + '-' + (++uid);
    var P = function (role) { return role === 'map' ? rect(-MARGIN, -MARGIN, w + 2 * MARGIN, h + 2 * MARGIN) : ''; };
    var u = { blur: function (v) { return v; }, scale: function (v) { return round(v, 3); } };
    var gph = materialGraph('m', 'SourceGraphic', this.maps.field, this.maps.scale, o, dark, P, u, false);
    var pad = 80 + this.refraction;
    var old = this.filter;
    this.filter = makeFilter(fid, {
      x: -pad, y: -pad, width: w + 2 * pad, height: h + 2 * pad,
      filterUnits: 'userSpaceOnUse', primitiveUnits: 'userSpaceOnUse',
      'color-interpolation-filters': 'sRGB'
    }, gph.markup);
    var bf = 'url(#' + fid + ')';
    rs.backdropFilter = bf;
    rs.webkitBackdropFilter = bf;
    if (old) win.requestAnimationFrame(function () { old.remove(); });
  };

  Glass.prototype.bindInteractive = function () {
    if (this.springs) return;
    var self = this, node = this.el;
    var sp = this.springs = {
      s: new Spring(1, { stiffness: 380, damping: 20 }),
      p: new Spring(0, { stiffness: 260, damping: 26 }),
      x: new Spring(0, { stiffness: 320, damping: 22 }),
      y: new Spring(0, { stiffness: 320, damping: 22 })
    };
    var start = null, size = 0, maxOff = 0;
    node.classList.add('lg-interactive');
    function soft(v) { return Math.sign(v) * maxOff * (1 - Math.exp(-Math.abs(v) / (maxOff * 4))); }
    function glowAt(e) {
      var r = node.getBoundingClientRect();
      node.style.setProperty('--lg-px', round((e.clientX - r.left) / r.width * 100, 1) + '%');
      node.style.setProperty('--lg-py', round((e.clientY - r.top) / r.height * 100, 1) + '%');
    }
    var tick = function (dt) {
      var a = sp.s.step(dt), b = sp.p.step(dt), c = sp.x.step(dt), d = sp.y.step(dt);
      var s = sp.s.value, ox = sp.x.value, oy = sp.y.value;
      var w = node.offsetWidth || 1, h = node.offsetHeight || 1;
      var sx = s * (1 + Math.abs(ox) / w * 0.5 - Math.abs(oy) / h * 0.15);
      var sy = s * (1 + Math.abs(oy) / h * 0.5 - Math.abs(ox) / w * 0.15);
      node.style.translate = round(ox, 2) + 'px ' + round(oy, 2) + 'px';
      node.style.scale = round(sx, 4) + ' ' + round(sy, 4);
      node.style.setProperty('--lg-press', round(sp.p.value, 3));
      if (self.mode === 'member') wakeGroupOf(node);
      var alive = a || b || c || d;
      if (!alive && !start) { node.style.translate = ''; node.style.scale = ''; }
      return alive || !!start;
    };
    this._down = function (e) {
      if (e.button > 0 || node.disabled) return;
      var w = node.offsetWidth, h = node.offsetHeight;
      size = Math.min(w, h);
      maxOff = clamp(size * 0.14, 3, 9);
      start = { x: e.clientX, y: e.clientY };
      glowAt(e);
      sp.s.to(1 + clamp(8 / Math.max(28, size), 0.015, 0.16));
      sp.p.to(1);
      animate(tick);
    };
    this._hover = function (e) { if (!start) glowAt(e); };
    node.addEventListener('pointermove', this._hover, { passive: true });
    this._move = function (e) {
      if (!start) return;
      glowAt(e);
      if (mqReduceMotion.matches || node.classList.contains('lg-draggable')) return;
      sp.x.to(soft(e.clientX - start.x)); sp.y.to(soft(e.clientY - start.y));
    };
    this._up = function () {
      if (!start) return;
      start = null;
      sp.s.to(1); sp.p.to(0); sp.x.to(0); sp.y.to(0);
      animate(tick);
    };
    node.addEventListener('pointerdown', this._down);
    win.addEventListener('pointermove', this._move, { passive: true });
    win.addEventListener('pointerup', this._up);
    win.addEventListener('pointercancel', this._up);
    node.addEventListener('keydown', function (e) { if (e.key === ' ' || e.key === 'Enter') { sp.s.to(1.04); sp.p.to(0.6); animate(tick); } });
    node.addEventListener('keyup', function () { sp.s.to(1); sp.p.to(0); animate(tick); });
  };

  Glass.prototype.destroy = function () {
    this.ro.disconnect();
    if (this.filter) this.filter.remove();
    if (this.scene) this.scene.remove(this);
    if (this.layers.parentNode) this.layers.parentNode.removeChild(this.layers);
    if (this._down) {
      this.el.removeEventListener('pointerdown', this._down);
      this.el.removeEventListener('pointermove', this._hover);
      win.removeEventListener('pointermove', this._move);
      win.removeEventListener('pointerup', this._up);
      win.removeEventListener('pointercancel', this._up);
    }
    this.el.classList.remove('lg-ready', 'lg-interactive');
    this.el.removeAttribute('data-lg-mode');
    instances.delete(this.el);
  };

  /* ------------------------------------------------------------------ *
   * Components
   * ------------------------------------------------------------------ */

  var wired = new WeakSet();

  // Tab bar / segmented control. The selection is a liquid droplet: its two
  // edges ride separate springs (the leading edge darts ahead, the trailing
  // edge catches up), so it stretches across the tabs and snaps back into a
  // pill. While touched it lifts into a magnifying lens you can drag.
  function tabs(nav) {
    if (wired.has(nav)) return; wired.add(nav);
    var list = nav.querySelector('.lg-tabs-list') || nav;
    var items = Array.prototype.slice.call(list.querySelectorAll('[role="tab"], .lg-tab'));
    if (!items.length) return;
    var ind = el('span', 'lg-tabs-indicator');
    ind.setAttribute('aria-hidden', 'true');
    list.appendChild(ind);
    apply(ind, {
      variant: 'clear', refraction: nav.dataset.lensRefraction ? +nav.dataset.lensRefraction : 6,
      magnify: +(nav.dataset.lensMagnify || 1.12), dispersion: +(nav.dataset.lensDispersion || 0)
    });
    var FAST = [640, 40], SLOW = [210, 24];
    var a = new Spring(0, { stiffness: FAST[0], damping: FAST[1] });
    var b = new Spring(0, { stiffness: FAST[0], damping: FAST[1] });
    var act = new Spring(0, { stiffness: 340, damping: 26 });
    var idx = Math.max(0, items.findIndex(function (t) { return t.getAttribute('aria-selected') === 'true'; }));
    var drag = null, settleTimer = 0;

    function edges(i) { var t = items[i]; return [t.offsetLeft, t.offsetLeft + t.offsetWidth]; }
    function tune(sp, k) { sp.stiffness = k[0]; sp.damping = k[1]; }
    function aim(L, R) {
      var dir = (L + R) / 2 - (a.target + b.target) / 2;
      if (Math.abs(dir) > 0.5 && !mqReduceMotion.matches) {
        tune(dir > 0 ? b : a, FAST); tune(dir > 0 ? a : b, SLOW);
      }
      a.to(L); b.to(R);
    }
    function place() { var e = edges(idx); a.set(e[0]); b.set(e[1]); render(); }
    function render() {
      var L = a.value, R = b.value, A = act.value;
      ind.style.translate = round(L, 2) + 'px 0';
      ind.style.width = round(Math.max(10, R - L), 2) + 'px';
      ind.style.scale = round(1 + A * 0.1, 4) + ' ' + round(1 + A * 0.24, 4);
      ind.style.setProperty('--lg-active', round(A, 3));
      ind.toggleAttribute('data-active', A > 0.02);
      var c = (L + R) / 2;
      items.forEach(function (t) {
        var tc = t.offsetLeft + t.offsetWidth / 2;
        t.style.setProperty('--lg-near', round(clamp(1 - Math.abs(tc - c) / t.offsetWidth, 0, 1), 3));
      });
    }
    var tick = function (dt) {
      var x = a.step(dt), y = b.step(dt), z = act.step(dt);
      render();
      return x || y || z || !!drag;
    };
    function select(i, fromUser) {
      i = clamp(i, 0, items.length - 1);
      var changed = i !== idx;
      idx = i;
      items.forEach(function (t, j) {
        t.setAttribute('aria-selected', j === i ? 'true' : 'false');
        t.tabIndex = j === i ? 0 : -1;
      });
      var e = edges(i);
      aim(e[0], e[1]);
      if (fromUser && changed) {
        act.to(1);
        clearTimeout(settleTimer);
        settleTimer = setTimeout(function () { if (!drag) { act.to(0); animate(tick); } }, 260);
        nav.dispatchEvent(new CustomEvent('lg:change', { detail: { index: i, tab: items[i] }, bubbles: true }));
      }
      animate(tick);
    }
    list.addEventListener('pointerdown', function (e) {
      if (e.button > 0) return;
      var lr = list.getBoundingClientRect();
      drag = { x0: e.clientX, moved: false, lr: lr, k: list.offsetWidth / lr.width };
      act.to(1);
      animate(tick);
    });
    win.addEventListener('pointermove', function (e) {
      if (!drag) return;
      if (Math.abs(e.clientX - drag.x0) > 3) drag.moved = true;
      if (!drag.moved) return;
      var w = items[idx].offsetWidth, c = (e.clientX - drag.lr.left) * drag.k;
      var min = items[0].offsetLeft + w / 2, max = items[items.length - 1].offsetLeft + items[items.length - 1].offsetWidth - w / 2;
      if (c < min) c = min - Math.sqrt(min - c) * 2;   // rubber-band past the ends
      if (c > max) c = max + Math.sqrt(c - max) * 2;
      aim(c - w / 2, c + w / 2);
    }, { passive: true });
    function end(e) {
      if (!drag) return;
      var d = drag; drag = null;
      if (d.moved) {
        var c = (a.target + b.target) / 2, best = 0, bd = Infinity;
        items.forEach(function (t, j) { var dd = Math.abs(t.offsetLeft + t.offsetWidth / 2 - c); if (dd < bd) { bd = dd; best = j; } });
        select(best, true);
      } else {
        var t = e && e.target && e.target.closest ? e.target.closest('[role="tab"], .lg-tab') : null;
        var j = items.indexOf(t);
        if (j >= 0) select(j, true);
      }
      setTimeout(function () { act.to(0); animate(tick); }, 120);
      animate(tick);
    }
    win.addEventListener('pointerup', end);
    win.addEventListener('pointercancel', end);
    list.addEventListener('keydown', function (e) {
      var k = e.key, n = null;
      if (k === 'ArrowRight' || k === 'ArrowDown') n = idx + 1;
      else if (k === 'ArrowLeft' || k === 'ArrowUp') n = idx - 1;
      else if (k === 'Home') n = 0; else if (k === 'End') n = items.length - 1;
      if (n == null) return;
      e.preventDefault(); select(n, true); items[idx].focus();
    });
    items.forEach(function (t, j) { t.tabIndex = j === idx ? 0 : -1; t.setAttribute('aria-selected', j === idx ? 'true' : 'false'); });
    new ResizeObserver(place).observe(list);
    place();
    nav.lgSelect = function (i) { select(i, true); };
  }

  // iOS 26-style switch: the knob turns into a liquid lens while touched.
  function toggle(label) {
    if (wired.has(label)) return; wired.add(label);
    var input = label.querySelector('input[type="checkbox"]');
    if (!input) return;
    input.setAttribute('role', 'switch');
    label.setAttribute('data-glass-scene', '');
    var track = el('span', 'lg-switch-track'); el('span', 'lg-switch-fill', track);
    track.setAttribute('data-glass-backdrop', '');
    var knob = el('span', 'lg-switch-knob');
    knob.setAttribute('aria-hidden', 'true');
    label.appendChild(track); label.appendChild(knob);
    apply(knob, { variant: 'clear', refraction: 3, magnify: 1.15 });
    var pos = new Spring(input.checked ? 1 : 0, { stiffness: 480, damping: 34 });
    var act = new Spring(0, { stiffness: 380, damping: 24 });
    var drag = null;
    function travel() { return track.offsetWidth - knob.offsetWidth - 4; }
    function render() {
      var p = pos.value, a = act.value, T = travel();
      var stretch = mqReduceMotion.matches ? 0 : clamp(Math.abs(pos.velocity) / 40, 0, 0.25);
      knob.style.translate = round(2 + p * T, 2) + 'px 0';
      knob.style.scale = round(1 + a * 0.48 + stretch, 4) + ' ' + round(1 + a * 0.42 - stretch * 0.3, 4);
      knob.style.setProperty('--lg-active', round(a, 3));
      knob.setAttribute('data-lg-lens', a > 0.02 ? 'on' : 'off');
      label.style.setProperty('--lg-on', round(clamp(p, 0, 1), 3));
    }
    var tick = function (dt) { var a = pos.step(dt), b = act.step(dt); render(); return a || b || !!drag; };
    var fromPointer = false;
    label.addEventListener('pointerdown', function (e) {
      if (e.button > 0 || input.disabled) return;
      fromPointer = true;
      drag = { x0: e.clientX, p0: pos.target, moved: false, k: label.offsetWidth / label.getBoundingClientRect().width };
      act.to(1); animate(tick);
    });
    win.addEventListener('pointermove', function (e) {
      if (!drag) return;
      var dx = (e.clientX - drag.x0) * drag.k;
      if (Math.abs(dx) > 3) drag.moved = true;
      if (drag.moved) pos.to(clamp(drag.p0 + dx / travel(), -0.08, 1.08));
    }, { passive: true });
    function end() {
      if (!drag) return;
      var d = drag; drag = null;
      var next = d.moved ? pos.target > 0.5 : !input.checked;
      if (next !== input.checked) {
        input.checked = next;
        input.dispatchEvent(new Event('input', { bubbles: true }));
        input.dispatchEvent(new Event('change', { bubbles: true }));
      }
      pos.to(next ? 1 : 0);
      setTimeout(function () { act.to(0); animate(tick); }, 90);
      animate(tick);
    }
    win.addEventListener('pointerup', end);
    win.addEventListener('pointercancel', end);
    label.addEventListener('click', function (e) {
      if (fromPointer) { e.preventDefault(); fromPointer = false; }
    });
    input.addEventListener('change', function () { pos.to(input.checked ? 1 : 0); animate(tick); });
    new ResizeObserver(render).observe(label);
    render();
  }

  // Slider: native <input type=range> for a11y, glass thumb while dragging.
  function slider(wrap) {
    if (wired.has(wrap)) return; wired.add(wrap);
    var input = wrap.querySelector('input[type="range"]');
    if (!input) return;
    wrap.setAttribute('data-glass-scene', '');
    var track = el('span', 'lg-slider-track'); var fill = el('span', 'lg-slider-fill', track);
    track.setAttribute('data-glass-backdrop', '');
    var knob = el('span', 'lg-slider-knob');
    knob.setAttribute('aria-hidden', 'true');
    wrap.insertBefore(track, input); wrap.insertBefore(knob, input);
    apply(knob, { variant: 'clear', refraction: 3, magnify: 1.15 });
    function frac() { var a = +input.min || 0, b = +input.max || 100; return (input.value - a) / ((b - a) || 1); }
    var pos = new Spring(frac(), { stiffness: 900, damping: 50 });
    var act = new Spring(0, { stiffness: 360, damping: 24 });
    var down = false;
    function render() {
      var W = wrap.offsetWidth, kw = knob.offsetWidth, p = pos.value;
      var stretch = mqReduceMotion.matches ? 0 : clamp(Math.abs(pos.velocity) / 9, 0, 0.3);
      knob.style.translate = round(p * (W - kw), 2) + 'px -50%';
      knob.style.scale = round(1 + act.value * 0.5 + stretch, 4) + ' ' + round(1 + act.value * 0.5 - stretch * 0.3, 4);
      knob.style.setProperty('--lg-active', round(act.value, 3));
      knob.setAttribute('data-lg-lens', act.value > 0.02 ? 'on' : 'off');
      fill.style.width = round(kw / 2 + p * (W - kw), 2) + 'px';
    }
    var tick = function (dt) { var a = pos.step(dt), b = act.step(dt); render(); return a || b || down; };
    input.addEventListener('input', function () { pos.to(frac()); animate(tick); });
    input.addEventListener('pointerdown', function () { down = true; act.to(1); animate(tick); });
    function up() { if (!down) return; down = false; act.to(0); animate(tick); }
    win.addEventListener('pointerup', up);
    win.addEventListener('pointercancel', up);
    new ResizeObserver(render).observe(wrap);
    render();
  }

  // Menu: a glass button that morphs into a glass panel.
  function menu(wrap) {
    if (wired.has(wrap)) return; wired.add(wrap);
    var trigger = wrap.querySelector('[aria-haspopup]') || wrap.querySelector('button');
    var panel = wrap.querySelector('.lg-menu-panel');
    if (!trigger || !panel) return;
    var items = Array.prototype.slice.call(panel.querySelectorAll('[role^="menuitem"], button, a'));
    var p = new Spring(0, { stiffness: 420, damping: 32 });
    var open = false;
    panel.hidden = true;
    trigger.setAttribute('aria-expanded', 'false');
    function geometry() {
      var tr = trigger.getBoundingClientRect(), wr = wrap.getBoundingClientRect();
      var below = (wrap.dataset.placement || 'bottom') !== 'top';
      panel.style.left = round(trigger.offsetLeft) + 'px';
      if (below) { panel.style.top = round(trigger.offsetTop) + 'px'; panel.style.bottom = ''; panel.style.transformOrigin = 'top left'; }
      else { panel.style.bottom = round(wrap.offsetHeight - trigger.offsetTop - trigger.offsetHeight) + 'px'; panel.style.top = ''; panel.style.transformOrigin = 'bottom left'; }
      return { tw: tr.width, th: tr.height, wr: wr };
    }
    var g = null;
    function render() {
      var v = clamp(p.value, -0.2, 1.2);
      var pw = panel.offsetWidth || 1, ph = panel.offsetHeight || 1;
      var sx = lerp(g.tw / pw, 1, v), sy = lerp(g.th / ph, 1, v);
      panel.style.scale = round(Math.max(0.01, sx), 4) + ' ' + round(Math.max(0.01, sy), 4);
      panel.style.setProperty('--lg-open', round(clamp(v, 0, 1), 3));
      trigger.style.setProperty('--lg-open', round(clamp(v, 0, 1), 3));
    }
    var tick = function (dt) {
      var alive = p.step(dt); render();
      if (!alive && !open) { panel.hidden = true; wrap.removeAttribute('data-open'); }
      return alive;
    };
    function setOpen(next, focus) {
      if (next === open) return;
      open = next;
      trigger.setAttribute('aria-expanded', String(open));
      if (open) {
        panel.hidden = false; wrap.setAttribute('data-open', '');
        g = geometry(); p.set(0); render(); p.to(1);
        if (focus !== false && items[0]) setTimeout(function () { items[0].focus({ preventScroll: true }); }, 60);
      } else {
        p.to(0);
        if (focus !== false) trigger.focus({ preventScroll: true });
      }
      animate(tick);
    }
    trigger.addEventListener('click', function () { setOpen(!open); });
    doc.addEventListener('pointerdown', function (e) { if (open && !wrap.contains(e.target)) setOpen(false, false); });
    wrap.addEventListener('keydown', function (e) {
      if (!open) return;
      var i = items.indexOf(doc.activeElement);
      if (e.key === 'Escape') { e.preventDefault(); setOpen(false); }
      else if (e.key === 'ArrowDown') { e.preventDefault(); items[(i + 1) % items.length].focus(); }
      else if (e.key === 'ArrowUp') { e.preventDefault(); items[(i - 1 + items.length) % items.length].focus(); }
    });
    items.forEach(function (it) {
      it.addEventListener('click', function () {
        wrap.dispatchEvent(new CustomEvent('lg:select', { detail: { item: it, value: it.dataset.value || it.textContent.trim() }, bubbles: true }));
        setOpen(false);
      });
    });
    wrap.lgOpen = function (v) { setOpen(v !== false); };
  }

  // Progressive blur / scroll-edge effect: stacked blur layers with feathered masks.
  function progressiveBlur(node) {
    if (wired.has(node)) return; wired.add(node);
    var n = +(node.dataset.layers || 6), max = +(node.dataset.blur || 24);
    var dir = node.dataset.direction || 'top';
    var to = { top: 'to top', bottom: 'to bottom', left: 'to left', right: 'to right' }[dir] || 'to top';
    var step = 100 / (n + 1);
    for (var i = 0; i < n; i++) {
      var s = el('span', 'lg-pb-layer', node);
      var b = round(max / Math.pow(2, n - 1 - i), 2);
      var a = i * step, c = (i + 1) * step, d = (i + 2) * step;
      var mask = 'linear-gradient(' + to + ', rgba(0,0,0,0) ' + round(a) + '%, #000 ' + round(c) + '%' + (i < n - 1 ? ', #000 ' + round(d) + '%, rgba(0,0,0,0) ' + round(Math.min(100, d + step)) + '%' : '') + ')';
      s.style.backdropFilter = s.style.webkitBackdropFilter = 'blur(' + b + 'px)';
      s.style.webkitMaskImage = s.style.maskImage = mask;
    }
    var tint = el('span', 'lg-pb-tint', node);
    tint.style.background = 'linear-gradient(' + to + ', transparent, var(--lg-pb-tint, rgba(0,0,0,.35)))';
  }

  // Draggable glass (lenses, droplets): throw it and it glides with momentum.
  function draggable(node) {
    if (wired.has(node)) return; wired.add(node);
    node.classList.add('lg-draggable');
    node.style.touchAction = 'none';
    var drag = null, vx = 0, vy = 0, lastT = 0;
    function bounds() {
      var p = node.offsetParent || doc.body;
      return { w: p.clientWidth - node.offsetWidth, h: p.clientHeight - node.offsetHeight };
    }
    function put(l, t) { node.style.left = round(l, 1) + 'px'; node.style.top = round(t, 1) + 'px'; node.style.right = 'auto'; node.style.bottom = 'auto'; wakeGroupOf(node); }
    node.addEventListener('pointerdown', function (e) {
      if (e.button > 0) return;
      // controls inside a draggable surface keep their own clicks
      var ctl = e.target.closest && e.target.closest('button, a, input, select, textarea, label, [role="slider"], [role="tab"]');
      if (ctl && ctl !== node && node.contains(ctl)) return;
      e.preventDefault();
      try { node.setPointerCapture(e.pointerId); } catch (_) {}
      drag = { x: e.clientX, y: e.clientY, l: node.offsetLeft, t: node.offsetTop, k: node.offsetWidth / node.getBoundingClientRect().width || 1 };
      vx = vy = 0; lastT = win.performance.now();
      node.setAttribute('data-dragging', '');
    });
    node.addEventListener('pointermove', function (e) {
      if (!drag) return;
      var now = win.performance.now(), dt = Math.max(8, now - lastT) / 1000;
      var b = bounds();
      var l = drag.l + (e.clientX - drag.x) * drag.k, t = drag.t + (e.clientY - drag.y) * drag.k;
      // rubber-band outside the parent
      if (l < 0) l = -Math.sqrt(-l) * 3; if (l > b.w) l = b.w + Math.sqrt(l - b.w) * 3;
      if (t < 0) t = -Math.sqrt(-t) * 3; if (t > b.h) t = b.h + Math.sqrt(t - b.h) * 3;
      vx = vx * 0.4 + (l - node.offsetLeft) / dt * 0.6; vy = vy * 0.4 + (t - node.offsetTop) / dt * 0.6;
      lastT = now;
      put(l, t);
    });
    function release() {
      if (!drag) return;
      drag = null;
      node.removeAttribute('data-dragging');
      if (mqReduceMotion.matches) { vx = vy = 0; }
      var x = node.offsetLeft, y = node.offsetTop;
      var sx = new Spring(x, { stiffness: 170, damping: 22 }), sy = new Spring(y, { stiffness: 170, damping: 22 });
      animate(function (dt) {
        if (drag) return false;
        var b = bounds();
        vx *= Math.exp(-dt * 4.5); vy *= Math.exp(-dt * 4.5);
        x += vx * dt; y += vy * dt;
        var cx = clamp(x, 0, b.w), cy = clamp(y, 0, b.h);
        if (cx !== x) { sx.value = x; sx.velocity = vx; sx.to(cx); x = cx; vx = 0; }
        if (cy !== y) { sy.value = y; sy.velocity = vy; sy.to(cy); y = cy; vy = 0; }
        var a = sx.step(dt), c = sy.step(dt);
        put(a ? sx.value : x, c ? sy.value : y);
        if (!a) sx.set(x); if (!c) sy.set(y);
        return a || c || Math.abs(vx) + Math.abs(vy) > 8;
      });
    }
    node.addEventListener('pointerup', release);
    node.addEventListener('pointercancel', release);
    node.addEventListener('keydown', function (e) {
      var d = e.shiftKey ? 40 : 10, m = { ArrowLeft: [-d, 0], ArrowRight: [d, 0], ArrowUp: [0, -d], ArrowDown: [0, d] }[e.key];
      if (!m) return;
      e.preventDefault();
      var b = bounds();
      put(clamp(node.offsetLeft + m[0], 0, b.w), clamp(node.offsetTop + m[1], 0, b.h));
    });
  }


  /* ------------------------------------------------------------------ *
   * Glass group — Apple's GlassEffectContainer. Every glass element inside
   * is rendered as ONE liquid surface: shapes are combined with a smooth
   * union of signed distance fields, so neighbours melt together with a
   * neck as they approach and pinch apart as they separate (the "morph").
   * The union is re-solved only while something moves.
   * ------------------------------------------------------------------ */

  var groups = new Map();
  // members that move or scale (springs, drags) keep their group awake
  function wakeGroupOf(node) {
    var g = node.parentElement && node.parentElement.closest(GROUP_SELECTOR);
    if (g && groups.has(g)) groups.get(g).wake();
  }

  function sdRoundBox(px, py, hw, hh, r) {
    var qx = Math.abs(px) - (hw - r), qy = Math.abs(py) - (hh - r);
    var ox = Math.max(qx, 0), oy = Math.max(qy, 0);
    return Math.sqrt(ox * ox + oy * oy) + Math.min(Math.max(qx, qy), 0) - r;
  }

  function Group(node, opts) {
    this.el = node; this.id = ++uid; this.userOpts = opts || {};
    this.opts = resolveOptions(node, this.userOpts);
    this.merge = +(node.dataset.glassMerge || this.userOpts.merge || 24);
    groups.set(node, this);
    node.classList.add('lg-group-ready');
    if (win.getComputedStyle(node).position === 'static') node.style.position = 'relative';
    var L = this.layer = el('span', 'lg-group-layer');
    L.setAttribute('aria-hidden', 'true');
    this.refract = el('span', 'lg-g-refract', L);
    this.fill = el('span', 'lg-g-fill', L);
    this.overlay = el('span', 'lg-g-overlay', L);
    node.insertBefore(L, node.firstChild);
    this.sig = '';
    var self = this;
    this.tick = function () { return self.update(); };
    this.idle = 0;
    this.visible = true;
    if ('IntersectionObserver' in win) {
      new IntersectionObserver(function (es) { self.visible = es[es.length - 1].isIntersecting; if (self.visible) self.wake(); }, { rootMargin: '100px' }).observe(node);
    }
    this.ro = new ResizeObserver(function () { self.wake(); });
    this.ro.observe(node);
    this.wake();
  }
  Group.prototype.wake = function () { this.idle = 0; animate(this.tick); };
  Group.prototype.members = function () {
    var self = this;
    return Array.prototype.filter.call(this.el.querySelectorAll('.lg-ready'), function (m) {
      return m.parentElement.closest(GROUP_SELECTOR) === self.el && m.offsetWidth && !m.hidden;
    });
  };
  Group.prototype.update = function () {
    if (!this.el.isConnected) return false;
    var ms = this.members();
    var gr = this.el.getBoundingClientRect();
    var k = gr.width ? this.el.offsetWidth / gr.width : 1;
    var shapes = ms.map(function (m) {
      var r = m.getBoundingClientRect(), lw = m.offsetWidth || 1;
      var sc = r.width / lw / k;
      return { x: (r.left - gr.left) * k, y: (r.top - gr.top) * k, w: r.width * k, h: r.height * k,
               r: radiusOf(m, m.offsetWidth, m.offsetHeight) * sc };
    });
    var dark = appearanceOf(this.el) === 'dark';
    var sig = shapes.map(function (s) { return [round(s.x, 1), round(s.y, 1), round(s.w, 1), round(s.h, 1), round(s.r, 1)].join(','); }).join(';') + '|' + dark;
    if (sig !== this.sig) { this.sig = sig; this.idle = 0; this.draw(shapes, dark); }
    else this.idle++;
    // keep polling briefly after the last change (springs/CSS transitions), then sleep
    return this.visible && this.idle < 45;
  };
  Group.prototype.draw = function (shapes, dark) {
    var L = this.layer, o = this.opts;
    if (!shapes.length) { L.style.display = 'none'; return; }
    L.style.display = '';
    this.el.toggleAttribute('data-lg-dark', dark);
    var P = Math.ceil(this.merge + 4);
    var x0 = Infinity, y0 = Infinity, x1 = -Infinity, y1 = -Infinity, minDim = Infinity;
    shapes.forEach(function (s) {
      x0 = Math.min(x0, s.x); y0 = Math.min(y0, s.y); x1 = Math.max(x1, s.x + s.w); y1 = Math.max(y1, s.y + s.h);
      minDim = Math.min(minDim, s.w, s.h);
    });
    x0 -= P; y0 -= P; x1 += P; y1 += P;
    var w = Math.ceil(x1 - x0), h = Math.ceil(y1 - y0);
    L.style.left = round(x0) + 'px'; L.style.top = round(y0) + 'px';
    L.style.width = w + 'px'; L.style.height = h + 'px';

    var R = autoRefraction(o, minDim, minDim);
    var s = clamp(Math.sqrt(180000 / (w * h)), 0.5, 1.5);
    var W = Math.max(2, Math.round(w * s)), H = Math.max(2, Math.round(h * s));
    var K = Math.max(1, this.merge * 2); // bridges form once the gap < merge
    var n = shapes.length, sh = shapes.map(function (q) {
      var r = Math.min(q.r, q.w / 2, q.h / 2);
      return { cx: q.x - x0 + q.w / 2, cy: q.y - y0 + q.h / 2, hw: q.w / 2, hh: q.h / 2, r: r };
    });
    // union signed distance field (polynomial smooth-min)
    var SD = new Float32Array(W * H);
    for (var j = 0; j < H; j++) {
      var py = (j + 0.5) / s;
      for (var i = 0; i < W; i++) {
        var px = (i + 0.5) / s, d = 1e9;
        for (var m = 0; m < n; m++) {
          var q = sh[m], dm = sdRoundBox(px - q.cx, py - q.cy, q.hw, q.hh, q.r);
          var hk = Math.max(K - Math.abs(d - dm), 0) / K;
          d = Math.min(d, dm) - hk * hk * K * 0.25;
        }
        SD[j * W + i] = d;
      }
    }
    var sample = function (x, y) {
      var i = clamp(Math.floor(x * s), 0, W - 1), j = clamp(Math.floor(y * s), 0, H - 1), ix = j * W + i;
      var gx = (SD[j * W + Math.min(W - 1, i + 1)] - SD[j * W + Math.max(0, i - 1)]) * s;
      var gy = (SD[Math.min(H - 1, j + 1) * W + i] - SD[Math.max(0, j - 1) * W + i]) * s;
      var gl = Math.sqrt(gx * gx + gy * gy) || 1;
      var sad = clamp((gl - 0.08) / 0.4, 0, 1); // fade the bend out at saddle points
      return [SD[ix], gx / gl, gy / gl, sad * sad * (3 - 2 * sad)];
    };
    var maps = renderMaps(W, H, s, sample, { refraction: R, bezel: o.bezel, magnify: 1, dark: dark });
    var mask = 'url("' + maps.mask + '")';
    [this.refract, this.fill].forEach(function (e) { e.style.webkitMaskImage = e.style.maskImage = mask; });
    this.overlay.style.backgroundImage = 'url("' + maps.overlay + '")';

    var rs = this.refract.style;
    if (supports.backdropRefraction && !mqReduceTransparency.matches && !mqMoreContrast.matches) {
      var Pm = function (role) { return role === 'map' ? rect(0, 0, w, h) : ''; };
      var u = { blur: function (v) { return v; }, scale: function (v) { return round(v, 3); } };
      var gph = materialGraph('g', 'SourceGraphic', maps.field, maps.scale, o, dark, Pm, u, false);
      var pad = 80 + R, old = this.filter;
      this.filter = makeFilter('lg-group-' + this.id + '-' + (++uid), {
        x: -pad, y: -pad, width: w + 2 * pad, height: h + 2 * pad, filterUnits: 'userSpaceOnUse', primitiveUnits: 'userSpaceOnUse',
        'color-interpolation-filters': 'sRGB'
      }, gph.markup);
      rs.backdropFilter = rs.webkitBackdropFilter = 'url(#' + this.filter.id + ')';
      if (old) old.remove();
    } else {
      rs.backdropFilter = rs.webkitBackdropFilter = o.variant === 'regular' ? 'blur(18px) saturate(1.4)' : 'blur(1px)';
      this.el.setAttribute('data-lg-frosted', '');
    }
  };

  function group(node) {
    if (groups.has(node)) return groups.get(node);
    return new Group(node);
  }

  var COMPONENTS = [
    ['.lg-tabs', tabs],
    ['.lg-switch', toggle],
    ['.lg-slider', slider],
    ['.lg-menu', menu],
    ['.lg-progressive-blur', progressiveBlur],
    ['[data-glass-draggable]', draggable],
    [GROUP_SELECTOR, group]
  ];

  /* ------------------------------------------------------------------ *
   * Public API
   * ------------------------------------------------------------------ */

  var GLASS_SELECTOR = '.glass, [data-glass]';

  function apply(node, opts) {
    if (!node || node.nodeType !== 1) return null;
    if (node.matches(GROUP_SELECTOR)) return group(node);
    var g = instances.get(node);
    if (g) return opts ? g.update(opts) : g;
    return new Glass(node, opts);
  }
  function destroy(node) { var g = instances.get(node); if (g) g.destroy(); }
  function scan(rootNode) {
    rootNode = rootNode || doc;
    COMPONENTS.forEach(function (c) {
      if (rootNode.matches && rootNode.matches(c[0])) c[1](rootNode);
      Array.prototype.forEach.call(rootNode.querySelectorAll ? rootNode.querySelectorAll(c[0]) : [], c[1]);
    });
    if (rootNode.matches && rootNode.matches(GLASS_SELECTOR)) apply(rootNode);
    Array.prototype.forEach.call(rootNode.querySelectorAll ? rootNode.querySelectorAll(GLASS_SELECTOR) : [], function (n) { apply(n); });
  }
  function rerenderAll() {
    mapCache.clear();
    doc.querySelectorAll('.lg-ready').forEach(function (n) { var g = instances.get(n); if (g) { g.lastSig = ''; g.render(); } });
    groups.forEach(function (g) { g.sig = ''; g.wake(); });
  }

  var observer = null, initialized = false;
  function init(opts) {
    if (opts) assign(defaults, opts);
    if (initialized) { scan(doc); return api; }
    initialized = true;
    doc.documentElement.classList.add('lg-js', supports.backdropRefraction ? 'lg-refraction-backdrop' : 'lg-refraction-scene');
    scan(doc);
    observer = new MutationObserver(function (muts) {
      var appearance = false;
      muts.forEach(function (m) {
        if (m.type === 'attributes') { appearance = true; return; }
        Array.prototype.forEach.call(m.addedNodes, function (n) { if (n.nodeType === 1 && !n.classList.contains('lg-layers')) scan(n); });
        Array.prototype.forEach.call(m.removedNodes, function (n) {
          if (n.nodeType !== 1) return;
          if (n.classList.contains('lg-layers')) {
            // innerHTML / textContent wiped the glass layers: put them back
            var g = instances.get(m.target);
            if (g && g.layers === n && m.target.isConnected && !n.parentNode) m.target.insertBefore(n, m.target.firstChild);
            return;
          }
          if (instances.has(n) && !n.isConnected) destroy(n);
          if (n.querySelectorAll) Array.prototype.forEach.call(n.querySelectorAll('.lg-ready'), function (c) { if (!c.isConnected) destroy(c); });
        });
      });
      if (appearance) rerenderAll();
    });
    observer.observe(doc.documentElement, { childList: true, subtree: true, attributes: true, attributeFilter: ['data-glass-appearance'] });
    [mqReduceTransparency, mqMoreContrast, mqDark].forEach(function (q) { if (q.addEventListener) q.addEventListener('change', rerenderAll); });
    return api;
  }

  var api = {
    version: VERSION,
    supports: supports,
    defaults: defaults,
    materials: MATERIALS,
    init: init,
    apply: apply,
    get: function (node) { return instances.get(node) || null; },
    destroy: destroy,
    refresh: function (node) {
      if (node) { var g = instances.get(node); if (g) { g.lastSig = ''; g.render(); } return; }
      scan(doc);
      rerenderAll();
    },
    maps: function (w, h, r, opts) {
      var o = resolveOptions(doc.createElement('div'), opts);
      return makeMaps(w, h, r, { refraction: autoRefraction(o, w, h), bezel: o.bezel, magnify: o.magnify, dark: !!(opts && opts.dark) });
    },
    Spring: Spring,
    animate: animate,
    group: group,
    components: { tabs: tabs, switch: toggle, slider: slider, menu: menu, progressiveBlur: progressiveBlur, draggable: draggable }
  };

  // Auto-init unless the script tag opts out with data-manual.
  var current = doc.currentScript;
  if (!(current && current.hasAttribute('data-manual'))) {
    if (doc.readyState === 'loading') doc.addEventListener('DOMContentLoaded', function () { init(); });
    else init();
  }

  return api;
});
