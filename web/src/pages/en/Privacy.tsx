import { LegalLayout } from '@/components/layout/LegalLayout'

export function Privacy() {
  return (
    <LegalLayout
      title="Privacy Policy"
      subtitle="OpenDisplay Android · last updated 2026-08-15"
      page="privacy"
      backLabel="Back to OpenDisplay Android"
    >
      <p>
        OpenDisplay Android is an open-source client that turns an Android device into a second
        monitor for a Mac running the original <a href="https://opendisplay.app/">OpenDisplay</a>{' '}
        app. This policy covers what the app does and does not do with your data.
      </p>

      <section>
        <h2>Short version</h2>
        <ul>
          <li>No account, no sign-up, no login.</li>
          <li>No analytics, no crash reporting, no advertising SDKs.</li>
          <li>No data is sent to any server operated by the developer — there isn't one.</li>
          <li>
            All network traffic is a direct connection between your Android device and your own
            Mac, over your local network (or a USB cable via <code>adb forward</code>).
          </li>
        </ul>
      </section>

      <section>
        <h2>What the app does on the network</h2>
        <p>
          The app listens on a local TCP port and advertises itself via mDNS (
          <code>_opensidecar._tcp</code>) so the OpenDisplay Mac app can find and connect to it on
          the same network. Once connected, it exchanges video frames (screen content from your
          Mac) and input events (touch/scroll you send back) directly with that Mac — nothing is
          relayed through, or stored on, any third-party or developer-owned server.
        </p>
      </section>

      <section>
        <h2>Permissions used</h2>
        <table>
          <tbody>
            <tr>
              <th>Permission</th>
              <th>Why</th>
            </tr>
            <tr>
              <td>
                <code>INTERNET</code>
              </td>
              <td>
                Open the local TCP socket the Mac connects to. Used only for the direct Mac
                connection described above — not for any internet service.
              </td>
            </tr>
            <tr>
              <td>
                <code>ACCESS_WIFI_STATE</code>, <code>CHANGE_WIFI_MULTICAST_STATE</code>
              </td>
              <td>mDNS discovery on the local WiFi network, so the Mac can find the device automatically.</td>
            </tr>
            <tr>
              <td>
                <code>FOREGROUND_SERVICE</code>, <code>FOREGROUND_SERVICE_CONNECTED_DEVICE</code>
              </td>
              <td>
                Keep the connection to the Mac alive while the app is acting as an external
                display, even if it's not the app on screen.
              </td>
            </tr>
            <tr>
              <td>
                <code>POST_NOTIFICATIONS</code>
              </td>
              <td>Show the required status notification for the foreground service above.</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section>
        <h2>Data storage</h2>
        <p>
          The only thing the app persists locally on the device is its own settings (the mDNS
          device name) — kept in local app preferences, never transmitted anywhere, and removed if
          you uninstall the app.
        </p>
      </section>

      <section>
        <h2>Third parties</h2>
        <p>None. The app has no third-party SDKs, no ad network, no analytics provider, no cloud backend.</p>
      </section>

      <section>
        <h2>Changes to this policy</h2>
        <p>
          Any change to what the app does with data will be reflected on this page, with the "last
          updated" date above.
        </p>
      </section>

      <section>
        <h2>Contact</h2>
        <p>
          Questions or concerns: open an issue on the{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/issues">
            GitHub repository
          </a>
          .
        </p>
      </section>
    </LegalLayout>
  )
}
