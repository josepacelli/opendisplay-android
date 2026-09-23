import { LegalLayout } from '@/components/layout/LegalLayout'

export function Contributing() {
  return (
    <LegalLayout
      title="Como contribuir"
      subtitle="OpenDisplay Android"
      page="contributing"
      backLabel="Voltar ao OpenDisplay Android"
    >
      <p>
        Obrigado por considerar contribuir. Este é um projeto pessoal, mantido nos tempos livres —
        o tempo de resposta varia, mas relatos de bugs e pull requests são mesmo bem-vindos.
      </p>

      <section>
        <h2>Código de Conduta</h2>
        <p>
          Este projeto segue o <a href="code-of-conduct.html">Contributor Covenant</a>. Ao
          participar, espera-se que o respeite.
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
            <strong>Prova.</strong> Uma captura de ecrã, output do <code>adb logcat</code>, ou um
            comando + a sua saída mostrando o comportamento real. Oculte qualquer IP ou dado
            pessoal antes de colar.
          </li>
          <li>
            <strong>Aparelho e versão do Android</strong>, se for um bug.
          </li>
          <li>Verifique as issues existentes primeiro — pode já estar reportado.</li>
        </ul>
      </section>

      <section>
        <h2>Ambiente de desenvolvimento</h2>
        <pre>
          <code>{`./gradlew assembleDebug          # build
./gradlew installDebug           # instala num dispositivo/emulador ligado
./gradlew testDebugUnitTest      # corre os testes unitários
adb logcat -s OpenDisplay:*      # logs da app`}</code>
        </pre>
        <p>
          Requer o Android SDK instalado, com <code>ANDROID_HOME</code>/<code>local.properties</code>{' '}
          a apontar para ele. Veja o{' '}
          <a href="https://github.com/josepacelli/opendisplay-android#readme">README</a> para a
          documentação completa de build/uso.
        </p>
      </section>

      <section>
        <h2>Estilo de código</h2>
        <ul>
          <li>
            <strong>Sempre a solução mais simples que resolve a necessidade real.</strong> Sem
            abstrações especulativas, sem otimização prematura, sem flexibilidade não usada "para
            depois".
          </li>
          <li>
            <strong>Comentários</strong>: sem <code>{'//'}</code> soltos no meio do código. A
            documentação de classe/função vai em KDoc. O porquê não óbvio de uma linha ou bloco
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
          <code>jacocoCoverageVerification</code> no CI). UI, serviços e tudo o que depende de{' '}
          <code>MediaCodec</code>/sockets/<code>NsdManager</code> fica fora desse cálculo.
        </p>
        <pre>
          <code>{`./gradlew testDebugUnitTest
./gradlew jacocoCoverageVerification`}</code>
        </pre>
      </section>

      <section>
        <h2>Mensagens de commit</h2>
        <p>
          Escreva como se estivesse a explicar a mudança a uma pessoa, não a gerar um changelog —
          descreva o <em>porquê</em>, não só o <em>o quê</em>. Evite prefixos genéricos como{' '}
          <code>chore: ...</code> sem substância nenhuma atrás.
        </p>
      </section>

      <section>
        <h2>Pull requests</h2>
        <ol>
          <li>
            Crie a branch a partir da <code>main</code>.
          </li>
          <li>Mantenha a mudança focada — uma coisa por PR é mais fácil de rever e integrar.</li>
          <li>
            Confirme que <code>./gradlew assembleDebug</code> e{' '}
            <code>./gradlew testDebugUnitTest</code> passam localmente (
            <code>jacocoCoverageVerification</code> também, se mexeu em lógica pura).
          </li>
          <li>Abra o PR contra a <code>main</code> — o template vai pedir um test plan curto.</li>
          <li>O CI corre as mesmas verificações; um check vermelho precisa de ficar verde antes do merge.</li>
        </ol>
      </section>

      <section>
        <h2>Agradecimentos</h2>
        <p>
          <a href="https://github.com/VirgilChen97">@VirgilChen97</a> apontou a causa raiz certa
          (drenagem de saída do decoder presa ao próximo frame de entrada) num comentário no{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/issues/113">issue #113</a>,
          antes de qualquer linha da correção existir. Contribuição não precisa de ser código — um
          diagnóstico certeiro também conta.
        </p>
      </section>

      <section>
        <h2>Licença</h2>
        <p>
          Ao contribuir, concorda que a sua contribuição fica licenciada sob a{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/blob/main/LICENSE">
            licença GPL-3.0
          </a>{' '}
          deste projeto.
        </p>
      </section>
    </LegalLayout>
  )
}
