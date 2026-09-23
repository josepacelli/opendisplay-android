import { LegalLayout } from '@/components/layout/LegalLayout'

export function CodeOfConduct() {
  return (
    <LegalLayout
      title="Código de Conducta"
      subtitle="OpenDisplay Android · Contributor Covenant v2.1"
      page="code-of-conduct"
      backLabel="Volver a OpenDisplay Android"
    >
      <section>
        <h2>Nuestro compromiso</h2>
        <p>
          Como miembros, colaboradores y responsables de este proyecto, nos comprometemos a hacer
          que la participación en nuestra comunidad sea una experiencia libre de acoso para todos,
          independientemente de la edad, tamaño corporal, discapacidad visible o invisible, etnia,
          características sexuales, identidad y expresión de género, nivel de experiencia,
          educación, nivel socioeconómico, nacionalidad, apariencia personal, raza, religión, o
          identidad y orientación sexual.
        </p>
        <p className="mt-2.5">
          Nos comprometemos a actuar e interactuar de formas que contribuyan a una comunidad
          abierta, acogedora, diversa, inclusiva y sana.
        </p>
      </section>

      <section>
        <h2>Nuestros estándares</h2>
        <p>Ejemplos de comportamiento que contribuyen a un ambiente positivo:</p>
        <ul className="mt-2.5">
          <li>Mostrar empatía y amabilidad hacia otras personas</li>
          <li>Respetar opiniones, puntos de vista y experiencias diferentes</li>
          <li>Dar y aceptar con elegancia comentarios constructivos</li>
          <li>
            Asumir responsabilidad y disculparse con quienes se vean afectados por nuestros
            errores, aprendiendo de la experiencia
          </li>
          <li>
            Enfocarse en lo que es mejor no solo para nosotros como individuos, sino para la
            comunidad en general
          </li>
        </ul>
        <p className="mt-4">Ejemplos de comportamiento inaceptable:</p>
        <ul className="mt-2.5">
          <li>Uso de lenguaje o imágenes sexualizadas, y atención o insinuaciones sexuales de cualquier tipo</li>
          <li>Trolleo, comentarios insultantes o despectivos, y ataques personales o políticos</li>
          <li>Acoso público o privado</li>
          <li>Publicar información privada de terceros, como dirección física o de correo, sin permiso explícito</li>
          <li>Otra conducta que pueda considerarse razonablemente inapropiada en un entorno profesional</li>
        </ul>
      </section>

      <section>
        <h2>Responsabilidades de aplicación</h2>
        <p>
          El mantenedor del proyecto es responsable de aclarar y aplicar nuestros estándares de
          comportamiento aceptable, y tomará medidas correctivas apropiadas y justas en respuesta a
          cualquier comportamiento que considere inapropiado, amenazante, ofensivo o dañino.
        </p>
      </section>

      <section>
        <h2>Alcance</h2>
        <p>
          Este Código de Conducta aplica en todos los espacios de la comunidad (issues, pull
          requests, discusiones en este repositorio), y también cuando alguien representa
          oficialmente a la comunidad en espacios públicos.
        </p>
      </section>

      <section>
        <h2>Cómo reportar</h2>
        <p>
          Los casos de comportamiento abusivo, acosador o de otro modo inaceptable pueden
          reportarse al mantenedor en{' '}
          <a href="mailto:josepacelli@gmail.com">josepacelli@gmail.com</a>. Todas las denuncias
          serán revisadas e investigadas de forma pronta y justa. El mantenedor está obligado a
          respetar la privacidad y seguridad de quien reporte cualquier incidente.
        </p>
      </section>

      <section>
        <h2>Pautas de aplicación</h2>
        <p>
          <strong>1. Corrección</strong> — Uso de lenguaje inapropiado u otro comportamiento poco
          profesional. Consecuencia: una advertencia privada por escrito, explicando la violación.
        </p>
        <p className="mt-2.5">
          <strong>2. Advertencia</strong> — Una violación a través de un incidente único o una
          serie de acciones. Consecuencia: una advertencia con consecuencias por comportamiento
          continuado, incluyendo no interactuar con las personas involucradas por un período
          determinado.
        </p>
        <p className="mt-2.5">
          <strong>3. Suspensión temporal</strong> — Una violación seria de los estándares de la
          comunidad. Consecuencia: una suspensión temporal de cualquier interacción o comunicación
          pública con la comunidad.
        </p>
        <p className="mt-2.5">
          <strong>4. Suspensión permanente</strong> — Un patrón de violación, incluyendo acoso a un
          individuo o agresión hacia clases de personas. Consecuencia: una suspensión permanente
          de cualquier interacción pública dentro de la comunidad.
        </p>
      </section>

      <section>
        <h2>Atribución</h2>
        <p>
          Adaptado del{' '}
          <a href="https://www.contributor-covenant.org/version/2/1/code_of_conduct.html">
            Contributor Covenant
          </a>
          , versión 2.1. Texto completo y traducciones en{' '}
          <a href="https://www.contributor-covenant.org">contributor-covenant.org</a>.
        </p>
      </section>
    </LegalLayout>
  )
}
