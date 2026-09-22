import { LegalLayout } from '@/components/layout/LegalLayout'

export function Contributing() {
  return (
    <LegalLayout title="Como contribuir" subtitle="OpenDisplay Android">
      <p>
        Obrigado por considerar contribuir. Este é um projeto pessoal, mantido nas horas vagas —
        o tempo de resposta varia, mas relatos de bugs e pull requests são bem-vindos de verdade.
      </p>

      <section>
        <h2>Código de Conduta</h2>
        <p>
          Este projeto segue o <a href="code-of-conduct.html">Contributor Covenant</a>. Ao
          participar, espera-se que você o respeite.
        </p>
      </section>

      <section>
        <h2>Reportar bugs / propor funcionalidades</h2>
        <p>
          Abra uma issue no{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/issues">GitHub</a> usando o
          template de bug ou de feature request. Algumas coisas que aceleram muito a análise:
        </p>
        <ul className="mt-2.5">
          <li>
            <strong>Prova.</strong> Um screenshot, output do <code>adb logcat</code>, ou um
            comando + sua saída mostrando o comportamento real. Mascare qualquer IP ou dado
            pessoal antes de colar.
          </li>
          <li>
            <strong>Aparelho e versão do Android</strong>, se for um bug.
          </li>
          <li>Confira as issues existentes primeiro — pode já estar reportado.</li>
        </ul>
      </section>

      <section>
        <h2>Ambiente de desenvolvimento</h2>
        <pre>
          <code>{`./gradlew assembleDebug          # build
./gradlew installDebug           # instala num device/emulador conectado
./gradlew testDebugUnitTest      # roda os testes unitários
adb logcat -s OpenDisplay:*      # logs do app`}</code>
        </pre>
        <p>
          Requer Android SDK instalado, com <code>ANDROID_HOME</code>/<code>local.properties</code>{' '}
          apontando pra ele. Veja o{' '}
          <a href="https://github.com/josepacelli/opendisplay-android#readme">README</a> pra
          documentação completa de build/uso.
        </p>
      </section>

      <section>
        <h2>Estilo de código</h2>
        <ul>
          <li>
            <strong>Sempre a solução mais simples que resolve a necessidade real.</strong> Sem
            abstrações especulativas, sem otimização prematura, sem flexibilidade não usada "pra
            depois".
          </li>
          <li>
            <strong>Comentários</strong>: sem <code>{'//'}</code> solto no meio do código.
            Documentação de classe/função vai em KDoc. O porquê não óbvio de uma linha ou bloco
            vai em <code>RATIONALE.md</code>, não inline.
          </li>
          <li>
            Kotlin + Jetpack Compose, seguindo a estrutura já existente em{' '}
            <code>app/src/main/java/</code>.
          </li>
        </ul>
      </section>

      <section>
        <h2>Testes e cobertura</h2>
        <p>
          Lógica pura (parsers, constantes de protocolo, classes que não dependem do framework
          Android) precisa de testes unitários mantendo{' '}
          <strong>cobertura de linha em 95% ou mais</strong> (verificado por{' '}
          <code>jacocoCoverageVerification</code> no CI). UI, services e tudo que depende de{' '}
          <code>MediaCodec</code>/sockets/<code>NsdManager</code> ficam fora desse cálculo.
        </p>
        <pre>
          <code>{`./gradlew testDebugUnitTest
./gradlew jacocoCoverageVerification`}</code>
        </pre>
      </section>

      <section>
        <h2>Mensagens de commit</h2>
        <p>
          Escreva como se estivesse explicando a mudança pra uma pessoa, não gerando um changelog
          — descreva o <em>porquê</em>, não só o <em>o quê</em>. Evite prefixos genéricos tipo{' '}
          <code>chore: ...</code> sem substância nenhuma atrás.
        </p>
      </section>

      <section>
        <h2>Pull requests</h2>
        <ol>
          <li>
            Crie a branch a partir da <code>main</code>.
          </li>
          <li>Mantenha a mudança focada — uma coisa por PR é mais fácil de revisar e mergear.</li>
          <li>
            Confira que <code>./gradlew assembleDebug</code> e{' '}
            <code>./gradlew testDebugUnitTest</code> passam localmente (
            <code>jacocoCoverageVerification</code> também, se mexeu em lógica pura).
          </li>
          <li>Abra o PR contra a <code>main</code> — o template vai pedir um test plan curto.</li>
          <li>O CI roda as mesmas checagens; um check vermelho precisa ficar verde antes do merge.</li>
        </ol>
      </section>

      <section>
        <h2>Licença</h2>
        <p>
          Ao contribuir, você concorda que sua contribuição fica licenciada sob a{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/blob/main/LICENSE">
            licença GPL-3.0
          </a>{' '}
          deste projeto.
        </p>
      </section>
    </LegalLayout>
  )
}
