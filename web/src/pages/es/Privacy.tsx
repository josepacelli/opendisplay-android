import { LegalLayout } from '@/components/layout/LegalLayout'

export function Privacy() {
  return (
    <LegalLayout
      title="Política de Privacidad"
      subtitle="OpenDisplay Android · última actualización 2026-08-15"
      page="privacy"
      backLabel="Volver a OpenDisplay Android"
    >
      <p>
        OpenDisplay Android es un cliente de código abierto que convierte un dispositivo Android
        en un segundo monitor para un Mac que ejecuta la app original de{' '}
        <a href="https://opendisplay.app/">OpenDisplay</a>. Esta política cubre qué hace y qué no
        hace la app con tus datos.
      </p>

      <section>
        <h2>Resumen</h2>
        <ul>
          <li>Sin cuenta, sin registro, sin inicio de sesión.</li>
          <li>Sin analíticas, sin reporte de fallos, sin SDKs de publicidad.</li>
          <li>No se envían datos a ningún servidor operado por el desarrollador — no existe tal servidor.</li>
          <li>
            Todo el tráfico de red es una conexión directa entre tu dispositivo Android y tu
            propio Mac, por tu red local (o un cable USB vía <code>adb forward</code>).
          </li>
        </ul>
      </section>

      <section>
        <h2>Qué hace la app en la red</h2>
        <p>
          La app escucha en un puerto TCP local y se anuncia por mDNS (
          <code>_opensidecar._tcp</code>) para que la app de Mac de OpenDisplay pueda encontrarla
          y conectarse en la misma red. Una vez conectada, intercambia fotogramas de vídeo
          (contenido de pantalla de tu Mac) y eventos de entrada (toque/desplazamiento que envías
          de vuelta) directamente con ese Mac — nada se retransmite ni se almacena en ningún
          servidor de terceros o del desarrollador.
        </p>
      </section>

      <section>
        <h2>Permisos usados</h2>
        <table>
          <tbody>
            <tr>
              <th>Permiso</th>
              <th>Por qué</th>
            </tr>
            <tr>
              <td>
                <code>INTERNET</code>
              </td>
              <td>
                Abre el socket TCP local al que se conecta el Mac. Se usa solo para la conexión
                directa con el Mac descrita arriba — no para ningún servicio de internet.
              </td>
            </tr>
            <tr>
              <td>
                <code>ACCESS_WIFI_STATE</code>, <code>CHANGE_WIFI_MULTICAST_STATE</code>
              </td>
              <td>Descubrimiento por mDNS en la red WiFi local, para que el Mac encuentre el dispositivo automáticamente.</td>
            </tr>
            <tr>
              <td>
                <code>FOREGROUND_SERVICE</code>, <code>FOREGROUND_SERVICE_CONNECTED_DEVICE</code>
              </td>
              <td>
                Mantiene viva la conexión con el Mac mientras la app actúa como pantalla externa,
                aunque no sea la app en primer plano.
              </td>
            </tr>
            <tr>
              <td>
                <code>POST_NOTIFICATIONS</code>
              </td>
              <td>Muestra la notificación de estado requerida para el servicio en primer plano de arriba.</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section>
        <h2>Almacenamiento de datos</h2>
        <p>
          Lo único que la app guarda localmente en el dispositivo son sus propios ajustes (el
          nombre mDNS del dispositivo) — guardado en las preferencias locales de la app, nunca
          transmitido a ningún sitio, y eliminado si desinstalas la app.
        </p>
      </section>

      <section>
        <h2>Terceros</h2>
        <p>
          Ninguno. La app no tiene SDKs de terceros, ni red publicitaria, ni proveedor de
          analíticas, ni backend en la nube.
        </p>
      </section>

      <section>
        <h2>Cambios a esta política</h2>
        <p>
          Cualquier cambio en lo que la app hace con los datos se reflejará en esta página, con la
          fecha de "última actualización" de arriba.
        </p>
      </section>

      <section>
        <h2>Contacto</h2>
        <p>
          Preguntas o dudas: abre un issue en el{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/issues">
            repositorio de GitHub
          </a>
          .
        </p>
      </section>
    </LegalLayout>
  )
}
