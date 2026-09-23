import { LegalLayout } from '@/components/layout/LegalLayout'

export function Privacy() {
  return (
    <LegalLayout
      title="隐私政策"
      subtitle="OpenDisplay Android · 最后更新于 2026-08-15"
      page="privacy"
      backLabel="返回 OpenDisplay Android"
    >
      <p>
        OpenDisplay Android 是一个开源客户端，把安卓设备变成运行原版{' '}
        <a href="https://opendisplay.app/">OpenDisplay</a> 应用的 Mac 的第二显示器。本政策说明该应用
        对你的数据会做什么、不会做什么。
      </p>

      <section>
        <h2>简要说明</h2>
        <ul>
          <li>无需账号、无需注册、无需登录。</li>
          <li>没有分析统计，没有崩溃报告，没有广告 SDK。</li>
          <li>不会向开发者运营的任何服务器发送数据 — 因为根本没有这样的服务器。</li>
          <li>
            所有网络流量都是你的安卓设备与你自己的 Mac 之间的直接连接，走的是你的本地网络（或者通过{' '}
            <code>adb forward</code> 走 USB 数据线）。
          </li>
        </ul>
      </section>

      <section>
        <h2>应用在网络上做什么</h2>
        <p>
          应用在本地 TCP 端口上监听，并通过 mDNS（<code>_opensidecar._tcp</code>）广播自己，
          这样 OpenDisplay Mac 应用就能在同一网络中找到并连接它。连接建立后，它直接与那台 Mac 交换视频帧
          （来自你 Mac 的屏幕内容）和输入事件（你发回去的触摸/滚动）— 不经过、也不存储在任何第三方或开发者的
          服务器上。
        </p>
      </section>

      <section>
        <h2>使用的权限</h2>
        <table>
          <tbody>
            <tr>
              <th>权限</th>
              <th>用途</th>
            </tr>
            <tr>
              <td>
                <code>INTERNET</code>
              </td>
              <td>打开 Mac 连接所用的本地 TCP 套接字。仅用于上文所述与 Mac 的直接连接 — 不用于任何互联网服务。</td>
            </tr>
            <tr>
              <td>
                <code>ACCESS_WIFI_STATE</code>, <code>CHANGE_WIFI_MULTICAST_STATE</code>
              </td>
              <td>在本地 WiFi 网络上进行 mDNS 发现，让 Mac 能自动找到该设备。</td>
            </tr>
            <tr>
              <td>
                <code>FOREGROUND_SERVICE</code>, <code>FOREGROUND_SERVICE_CONNECTED_DEVICE</code>
              </td>
              <td>在应用充当外接显示器期间保持与 Mac 的连接，即使当前屏幕上显示的不是这个应用。</td>
            </tr>
            <tr>
              <td>
                <code>POST_NOTIFICATIONS</code>
              </td>
              <td>显示上述前台服务所需的状态通知。</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section>
        <h2>数据存储</h2>
        <p>
          应用在设备本地唯一保存的东西是它自己的设置（mDNS 设备名称）— 保存在应用本地的
          偏好设置中，从不向外传输，卸载应用后也会一并删除。
        </p>
      </section>

      <section>
        <h2>第三方</h2>
        <p>没有。应用没有任何第三方 SDK、广告网络、分析服务商，也没有云端后端。</p>
      </section>

      <section>
        <h2>本政策的变更</h2>
        <p>应用对数据处理方式的任何变化都会体现在本页面上，并更新上方的"最后更新"日期。</p>
      </section>

      <section>
        <h2>联系方式</h2>
        <p>
          有疑问或顾虑：请在{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/issues">GitHub 仓库</a>提交 issue。
        </p>
      </section>
    </LegalLayout>
  )
}
