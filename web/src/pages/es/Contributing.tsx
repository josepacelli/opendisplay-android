import { LegalLayout } from '@/components/layout/LegalLayout'

export function Contributing() {
  return (
    <LegalLayout
      title="Cómo contribuir"
      subtitle="OpenDisplay Android"
      page="contributing"
      backLabel="Volver a OpenDisplay Android"
    >
      <p>
        Gracias por considerar contribuir. Este es un proyecto personal, mantenido en tiempo libre
        — el tiempo de respuesta varía, pero los reportes de bugs y pull requests son bienvenidos
        de verdad.
      </p>

      <section>
        <h2>Código de Conducta</h2>
        <p>
          Este proyecto sigue el <a href="code-of-conduct.html">Contributor Covenant</a>. Al
          participar, se espera que lo respetes.
        </p>
      </section>

      <section>
        <h2>Reportar bugs / proponer funciones</h2>
        <p>
          Abre un issue en{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/issues">GitHub</a> usando la
          plantilla de bug o de feature request. Algunas cosas que aceleran mucho el análisis:
        </p>
        <ul className="mt-2.5">
          <li>
            <strong>Prueba.</strong> Una captura de pantalla, salida de <code>adb logcat</code>, o
            un comando + su salida mostrando el comportamiento real. Oculta cualquier IP o dato
            personal antes de pegar.
          </li>
          <li>
            <strong>Dispositivo y versión de Android</strong>, si es un bug.
          </li>
          <li>Revisa los issues existentes primero — puede que ya esté reportado.</li>
        </ul>
      </section>

      <section>
        <h2>Entorno de desarrollo</h2>
        <pre>
          <code>{`./gradlew assembleDebug          # build
./gradlew installDebug           # instala en un dispositivo/emulador conectado
./gradlew testDebugUnitTest      # corre los tests unitarios
adb logcat -s OpenDisplay:*      # logs de la app`}</code>
        </pre>
        <p>
          Requiere el Android SDK instalado, con <code>ANDROID_HOME</code>/
          <code>local.properties</code> apuntando a él. Consulta el{' '}
          <a href="https://github.com/josepacelli/opendisplay-android#readme">README</a> para la
          documentación completa de build/uso.
        </p>
      </section>

      <section>
        <h2>Estilo de código</h2>
        <ul>
          <li>
            <strong>Siempre la solución más simple que resuelve la necesidad real.</strong> Sin
            abstracciones especulativas, sin optimización prematura, sin flexibilidad sin usar
            "para después".
          </li>
          <li>
            <strong>Comentarios</strong>: sin <code>{'//'}</code> sueltos en medio del código. La
            documentación de clase/función va en KDoc. El porqué no obvio de una línea o bloque va
            en <code>RATIONALE.md</code>, no inline.
          </li>
          <li>
            Kotlin + Jetpack Compose, siguiendo la estructura ya existente en{' '}
            <code>app/src/main/java/</code>.
          </li>
        </ul>
      </section>

      <section>
        <h2>Tests y cobertura</h2>
        <p>
          La lógica pura (parsers, constantes de protocolo, clases que no dependen del framework
          de Android) necesita tests unitarios manteniendo{' '}
          <strong>cobertura de línea en 95% o más</strong> (verificado por{' '}
          <code>jacocoCoverageVerification</code> en CI). UI, servicios y todo lo que dependa de{' '}
          <code>MediaCodec</code>/sockets/<code>NsdManager</code> queda fuera de ese cálculo.
        </p>
        <pre>
          <code>{`./gradlew testDebugUnitTest
./gradlew jacocoCoverageVerification`}</code>
        </pre>
      </section>

      <section>
        <h2>Mensajes de commit</h2>
        <p>
          Escríbelos como si le estuvieras explicando el cambio a una persona, no generando un
          changelog — describe el <em>por qué</em>, no solo el <em>qué</em>. Evita prefijos
          genéricos como <code>chore: ...</code> sin sustancia detrás.
        </p>
      </section>

      <section>
        <h2>Pull requests</h2>
        <ol>
          <li>
            Crea la rama a partir de <code>main</code>.
          </li>
          <li>Mantén el cambio enfocado — una cosa por PR es más fácil de revisar y mergear.</li>
          <li>
            Asegúrate de que <code>./gradlew assembleDebug</code> y{' '}
            <code>./gradlew testDebugUnitTest</code> pasen localmente (
            <code>jacocoCoverageVerification</code> también, si tocaste lógica pura).
          </li>
          <li>Abre el PR contra <code>main</code> — la plantilla pedirá un test plan corto.</li>
          <li>El CI corre las mismas verificaciones; un check en rojo debe pasar a verde antes del merge.</li>
        </ol>
      </section>

      <section>
        <h2>Licencia</h2>
        <p>
          Al contribuir, aceptas que tu contribución queda licenciada bajo la{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/blob/main/LICENSE">
            licencia GPL-3.0
          </a>{' '}
          de este proyecto.
        </p>
      </section>
    </LegalLayout>
  )
}
