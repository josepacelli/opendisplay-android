import { LegalLayout } from '@/components/layout/LegalLayout'

export function Privacy() {
  return (
    <LegalLayout
      title="Política de Privacidade"
      subtitle="OpenDisplay Android · última atualização em 15/08/2026"
      page="privacy"
      backLabel="Voltar ao OpenDisplay Android"
    >
      <p>
        OpenDisplay Android é um cliente open source que transforma um dispositivo Android num
        segundo monitor para um Mac a correr a aplicação original{' '}
        <a href="https://opendisplay.app/">OpenDisplay</a>. Esta política cobre o que a aplicação
        faz e não faz com os seus dados.
      </p>

      <section>
        <h2>Resumo rápido</h2>
        <ul>
          <li>Sem conta, sem registo, sem login.</li>
          <li>Sem analytics, sem relatório de falhas, sem SDKs de publicidade.</li>
          <li>Nenhum dado é enviado para servidor operado pelo programador — não existe um.</li>
          <li>
            Todo o tráfego de rede é uma ligação direta entre o seu dispositivo Android e o seu
            próprio Mac, pela sua rede local (ou um cabo USB via <code>adb forward</code>).
          </li>
        </ul>
      </section>

      <section>
        <h2>O que a aplicação faz na rede</h2>
        <p>
          A aplicação escuta numa porta TCP local e anuncia-se via mDNS (
          <code>_opensidecar._tcp</code>) para que a aplicação de Mac do OpenDisplay a possa
          encontrar e ligar-se na mesma rede. Depois de ligada, troca frames de vídeo (conteúdo do
          ecrã do seu Mac) e eventos de entrada (toque/deslocamento que envia de volta)
          diretamente com esse Mac — nada é encaminhado através de, ou armazenado em, qualquer
          servidor de terceiros ou do programador.
        </p>
      </section>

      <section>
        <h2>Permissões usadas</h2>
        <table>
          <tbody>
            <tr>
              <th>Permissão</th>
              <th>Porquê</th>
            </tr>
            <tr>
              <td>
                <code>INTERNET</code>
              </td>
              <td>
                Abre o socket TCP local ao qual o Mac se liga. Usado apenas para a ligação direta
                com o Mac descrita acima — não para qualquer serviço de internet.
              </td>
            </tr>
            <tr>
              <td>
                <code>ACCESS_WIFI_STATE</code>, <code>CHANGE_WIFI_MULTICAST_STATE</code>
              </td>
              <td>Descoberta por mDNS na rede WiFi local, para que o Mac encontre o dispositivo automaticamente.</td>
            </tr>
            <tr>
              <td>
                <code>FOREGROUND_SERVICE</code>, <code>FOREGROUND_SERVICE_CONNECTED_DEVICE</code>
              </td>
              <td>
                Mantém viva a ligação com o Mac enquanto a aplicação atua como monitor externo,
                mesmo que não seja a aplicação em primeiro plano.
              </td>
            </tr>
            <tr>
              <td>
                <code>POST_NOTIFICATIONS</code>
              </td>
              <td>Mostra a notificação de estado exigida pelo serviço em primeiro plano acima.</td>
            </tr>
          </tbody>
        </table>
      </section>

      <section>
        <h2>Armazenamento de dados</h2>
        <p>
          A única coisa que a aplicação guarda localmente no dispositivo são as suas próprias
          definições (o nome mDNS do dispositivo) — guardadas nas preferências locais da
          aplicação, nunca transmitidas para lado nenhum, e removidas se desinstalar a aplicação.
        </p>
      </section>

      <section>
        <h2>Terceiros</h2>
        <p>
          Nenhum. A aplicação não tem SDKs de terceiros, rede de publicidade, fornecedor de
          analytics, nem backend na nuvem.
        </p>
      </section>

      <section>
        <h2>Alterações a esta política</h2>
        <p>
          Qualquer alteração ao que a aplicação faz com os dados será refletida nesta página, com
          a data de "última atualização" acima.
        </p>
      </section>

      <section>
        <h2>Contacto</h2>
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
