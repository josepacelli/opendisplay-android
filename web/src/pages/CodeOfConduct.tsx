import { LegalLayout } from '@/components/layout/LegalLayout'

export function CodeOfConduct() {
  return (
    <LegalLayout title="Código de Conduta" subtitle="OpenDisplay Android · Contributor Covenant v2.1">
      <section>
        <h2>Nosso compromisso</h2>
        <p>
          Nós, como membros, contribuidores e responsáveis por este projeto, nos comprometemos a
          fazer da participação na nossa comunidade uma experiência livre de assédio para todos,
          independente de idade, porte físico, deficiência visível ou não, etnia, características
          sexuais, identidade e expressão de gênero, nível de experiência, educação, condição
          socioeconômica, nacionalidade, aparência pessoal, raça, religião, ou identidade e
          orientação sexual.
        </p>
        <p className="mt-2.5">
          Nos comprometemos a agir e interagir de formas que contribuam para uma comunidade
          aberta, acolhedora, diversa, inclusiva e saudável.
        </p>
      </section>

      <section>
        <h2>Nossos padrões</h2>
        <p>Exemplos de comportamento que contribuem para um ambiente positivo:</p>
        <ul className="mt-2.5">
          <li>Demonstrar empatia e gentileza com outras pessoas</li>
          <li>Respeitar opiniões, pontos de vista e experiências diferentes</li>
          <li>Dar e aceitar com graça feedback construtivo</li>
          <li>
            Assumir responsabilidade e pedir desculpas a quem for afetado por nossos erros,
            aprendendo com a experiência
          </li>
          <li>Focar no que é melhor não só pra nós individualmente, mas pra comunidade como um todo</li>
        </ul>
        <p className="mt-4">Exemplos de comportamento inaceitável:</p>
        <ul className="mt-2.5">
          <li>Uso de linguagem ou imagens sexualizadas, e atenção ou investidas sexuais de qualquer tipo</li>
          <li>Trollagem, comentários insultuosos ou depreciativos, e ataques pessoais ou políticos</li>
          <li>Assédio público ou privado</li>
          <li>
            Publicar informações privadas de terceiros, como endereço físico ou de email, sem
            permissão explícita
          </li>
          <li>Outra conduta que possa ser razoavelmente considerada inapropriada num ambiente profissional</li>
        </ul>
      </section>

      <section>
        <h2>Responsabilidades de aplicação</h2>
        <p>
          O mantenedor do projeto é responsável por esclarecer e aplicar nossos padrões de
          comportamento aceitável, tomando ação corretiva apropriada e justa em resposta a
          qualquer comportamento considerado inapropriado, ameaçador, ofensivo ou prejudicial.
        </p>
      </section>

      <section>
        <h2>Escopo</h2>
        <p>
          Este Código de Conduta se aplica a todos os espaços da comunidade (issues, pull
          requests, discussões neste repositório), e também quando alguém está representando
          oficialmente a comunidade em espaços públicos.
        </p>
      </section>

      <section>
        <h2>Como reportar</h2>
        <p>
          Casos de comportamento abusivo, assediador ou de outra forma inaceitável podem ser
          reportados ao mantenedor em <a href="mailto:josepacelli@gmail.com">josepacelli@gmail.com</a>.
          Todas as denúncias serão revisadas e investigadas de forma pronta e justa. O mantenedor
          é obrigado a respeitar a privacidade e segurança de quem fizer a denúncia.
        </p>
      </section>

      <section>
        <h2>Diretrizes de aplicação</h2>
        <p>
          <strong>1. Correção</strong> — Uso de linguagem inapropriada ou outro comportamento não
          profissional. Consequência: um aviso privado, por escrito, explicando a violação.
        </p>
        <p className="mt-2.5">
          <strong>2. Advertência</strong> — Uma violação através de um incidente único ou uma
          série de ações. Consequência: uma advertência com consequências para comportamento
          continuado, incluindo não interagir com as pessoas envolvidas por um período
          determinado.
        </p>
        <p className="mt-2.5">
          <strong>3. Banimento temporário</strong> — Uma violação séria dos padrões da comunidade.
          Consequência: um banimento temporário de qualquer interação ou comunicação pública com a
          comunidade.
        </p>
        <p className="mt-2.5">
          <strong>4. Banimento permanente</strong> — Um padrão de violação, incluindo assédio a um
          indivíduo ou agressão a classes de pessoas. Consequência: um banimento permanente de
          qualquer interação pública dentro da comunidade.
        </p>
      </section>

      <section>
        <h2>Atribuição</h2>
        <p>
          Adaptado do{' '}
          <a href="https://www.contributor-covenant.org/version/2/1/code_of_conduct.html">
            Contributor Covenant
          </a>
          , versão 2.1. Texto completo e traduções em{' '}
          <a href="https://www.contributor-covenant.org">contributor-covenant.org</a>.
        </p>
      </section>
    </LegalLayout>
  )
}
