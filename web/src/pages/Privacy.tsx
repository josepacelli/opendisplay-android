import { LegalLayout } from '@/components/layout/LegalLayout'

export function Privacy() {
  return (
    <LegalLayout
      title="Política de Privacidade"
      subtitle="OpenDisplay Android · última atualização em 15/08/2026"
    >
      <p>
        OpenDisplay Android é um cliente open source que transforma um aparelho Android num
        segundo monitor pra um Mac rodando o app original{' '}
        <a href="https://opendisplay.app/">OpenDisplay</a>. Esta política cobre o que o app faz e
        não faz com os seus dados.
      </p>

      <section>
        <h2>Resumo rápido</h2>
        <ul>
          <li>Sem conta, sem cadastro, sem login.</li>
          <li>Sem analytics, sem relatório de erros, sem SDKs de anúncios.</li>
          <li>Nenhum dado é enviado a servidor operado pelo desenvolvedor — não existe um.</li>
          <li>
            Todo o tráfego de rede é uma conexão direta entre o seu aparelho Android e o seu
            próprio Mac, pela sua rede local (ou um cabo USB via <code>adb forward</code>).
          </li>
        </ul>
      </section>

      <section>
        <h2>O que o app faz na rede</h2>
        <p>
          O app escuta numa porta TCP local e se anuncia via mDNS (<code>_opensidecar._tcp</code>)
          pra que o app de Mac do OpenDisplay possa encontrá-lo e conectar na mesma rede. Depois
          de conectado, ele troca frames de vídeo (conteúdo da tela do seu Mac) e eventos de
          entrada (toque/scroll que você manda de volta) diretamente com aquele Mac — nada é
          repassado por, ou armazenado em, nenhum servidor de terceiros ou do desenvolvedor.
        </p>
      </section>

      <section>
        <h2>Permissões usadas</h2>
        <table>
          <tbody>
            <tr>
              <th>Permissão</th>
              <th>Por quê</th>
            </tr>
            <tr>
              <td>
                <code>INTERNET</code>
              </td>
              <td>
                Abre o socket TCP local ao qual o Mac se conecta. Usado só pra conexão direta com
                o Mac descrita acima — não pra nenhum serviço de internet.
              </td>
            </tr>
            <tr>
              <td>
                <code>ACCESS_WIFI_STATE</code>, <code>CHANGE_WIFI_MULTICAST_STATE</code>
              </td>
              <td>Descoberta por mDNS na rede WiFi local, pra que o Mac encontre o aparelho automaticamente.</td>
            </tr>
            <tr>
              <td>
                <code>FOREGROUND_SERVICE</code>, <code>FOREGROUND_SERVICE_CONNECTED_DEVICE</code>
              </td>
              <td>
                Mantém viva a conexão com o Mac enquanto o app atua como monitor externo, mesmo
                que não seja o app em primeiro plano.
              </td>
            </tr>
            <tr>
              <td>
                <code>POST_NOTIFICATIONS</code>
              </td>
              <td>Mostra a notificação de status exigida pelo serviço em primeiro plano acima.</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section>
        <h2>Armazenamento de dados</h2>
        <p>
          A única coisa que o app guarda localmente no aparelho são as suas próprias configurações
          (o nome mDNS do aparelho) — guardadas nas preferências locais do app, nunca transmitidas
          pra lugar nenhum, e removidas se você desinstalar o app.
        </p>
      </section>

      <section>
        <h2>Terceiros</h2>
        <p>
          Nenhum. O app não tem SDKs de terceiros, rede de anúncios, provedor de analytics, nem
          backend na nuvem.
        </p>
      </section>

      <section>
        <h2>Mudanças nesta política</h2>
        <p>
          Qualquer mudança no que o app faz com os dados vai ser refletida nesta página, com a
          data de "última atualização" acima.
        </p>
      </section>

      <section>
        <h2>Contato</h2>
        <p>
          Dúvidas ou preocupações: abra uma issue no{' '}
          <a href="https://github.com/josepacelli/opendisplay-android/issues">
            repositório do GitHub
          </a>
          .
        </p>
      </section>
    </LegalLayout>
  )
}
