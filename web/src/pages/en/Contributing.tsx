import { LegalLayout } from '@/components/layout/LegalLayout'

export function Contributing() {
  return (
    <LegalLayout
      title="Contributing"
      subtitle="OpenDisplay Android"
      page="contributing"
      backLabel="Back to OpenDisplay Android"
    >
      <p>
        Thanks for considering a contribution. This is a one-person side project maintained
        evenings and weekends — response times will vary, but bug reports and pull requests are
        genuinely welcome.
      </p>

      <section>
        <h2>Code of Conduct</h2>
        <p>
          This project follows the <a href="code-of-conduct.html">Contributor Covenant</a>. By
          participating, you're expected to uphold it.
        </p>
      </section>

      <section>
        <h2>Reporting bugs / requesting features</h2>
        <p>
          Open an issue on{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/issues">GitHub</a> using the
          bug report or feature request template. A few things that make a report much faster to
          act on:
        </p>
        <ul className="mt-2.5">
          <li>
            <strong>Proof.</strong> A screenshot, <code>adb logcat</code> output, or a command +
            its output showing the actual behavior. Mask any IP address or personal data before
            pasting.
          </li>
          <li>
            <strong>Device and Android version</strong>, if it's a bug.
          </li>
          <li>Check existing issues first — it may already be tracked.</li>
        </ul>
      </section>

      <section>
        <h2>Development setup</h2>
        <pre>
          <code>{`./gradlew assembleDebug          # build
./gradlew installDebug           # install on a connected device/emulator
./gradlew testDebugUnitTest      # run unit tests
adb logcat -s OpenDisplay:*      # app logs`}</code>
        </pre>
        <p>
          Requires the Android SDK installed, with <code>ANDROID_HOME</code>/
          <code>local.properties</code> pointing to it. See the{' '}
          <a href="https://github.com/josepacelli/opendisplay-android#readme">README</a> for the
          full build/usage docs.
        </p>
      </section>

      <section>
        <h2>Code style</h2>
        <ul>
          <li>
            <strong>Simplest solution that solves the real need.</strong> No speculative
            abstractions, no premature optimization, no unused flexibility "for later."
          </li>
          <li>
            <strong>Comments</strong>: no scattered <code>{'//'}</code> comments in the middle of
            code. Class/function documentation goes in KDoc. The non-obvious <em>why</em> behind a
            line or block goes in <code>RATIONALE.md</code>, not inline.
          </li>
          <li>
            Kotlin + Jetpack Compose, following the structure already in{' '}
            <code>app/src/main/java/</code>.
          </li>
        </ul>
      </section>

      <section>
        <h2>Tests and coverage</h2>
        <p>
          Pure logic (parsers, protocol constants, non-Android-framework classes) needs unit tests
          keeping <strong>line coverage at or above 95%</strong> (enforced by{' '}
          <code>jacocoCoverageVerification</code> in CI). UI, services, and anything requiring{' '}
          <code>MediaCodec</code>/sockets/<code>NsdManager</code> are excluded from that gate.
        </p>
        <pre>
          <code>{`./gradlew testDebugUnitTest
./gradlew jacocoCoverageVerification`}</code>
        </pre>
      </section>

      <section>
        <h2>Commit messages</h2>
        <p>
          Write them like you're explaining the change to a person, not a changelog generator —
          describe <em>why</em>, not just <em>what</em>. Avoid generic prefixes like{' '}
          <code>chore: ...</code> with no substance behind them.
        </p>
      </section>

      <section>
        <h2>Pull requests</h2>
        <ol>
          <li>
            Branch from <code>main</code>.
          </li>
          <li>Keep the change focused — one thing per PR is easier to review and merge.</li>
          <li>
            Make sure <code>./gradlew assembleDebug</code> and{' '}
            <code>./gradlew testDebugUnitTest</code> pass locally (
            <code>jacocoCoverageVerification</code> too, if you touched pure logic).
          </li>
          <li>Open the PR against <code>main</code> — the template will ask for a short test plan.</li>
          <li>CI runs the same checks; a red check needs to go green before merge.</li>
        </ol>
      </section>

      <section>
        <h2>License</h2>
        <p>
          By contributing, you agree your contribution is licensed under this project's{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/blob/main/LICENSE">
            GPL-3.0 license
          </a>
          .
        </p>
      </section>
    </LegalLayout>
  )
}
