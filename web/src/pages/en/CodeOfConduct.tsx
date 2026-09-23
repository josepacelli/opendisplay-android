import { LegalLayout } from '@/components/layout/LegalLayout'

export function CodeOfConduct() {
  return (
    <LegalLayout
      title="Code of Conduct"
      subtitle="OpenDisplay Android · Contributor Covenant v2.1"
      page="code-of-conduct"
      backLabel="Back to OpenDisplay Android"
    >
      <section>
        <h2>Our Pledge</h2>
        <p>
          We as members, contributors, and leaders pledge to make participation in our community a
          harassment-free experience for everyone, regardless of age, body size, visible or
          invisible disability, ethnicity, sex characteristics, gender identity and expression,
          level of experience, education, socio-economic status, nationality, personal appearance,
          race, religion, or sexual identity and orientation.
        </p>
        <p className="mt-2.5">
          We pledge to act and interact in ways that contribute to an open, welcoming, diverse,
          inclusive, and healthy community.
        </p>
      </section>

      <section>
        <h2>Our Standards</h2>
        <p>Examples of behavior that contributes to a positive environment:</p>
        <ul className="mt-2.5">
          <li>Demonstrating empathy and kindness toward other people</li>
          <li>Being respectful of differing opinions, viewpoints, and experiences</li>
          <li>Giving and gracefully accepting constructive feedback</li>
          <li>
            Accepting responsibility and apologizing to those affected by our mistakes, and
            learning from the experience
          </li>
          <li>Focusing on what is best not just for us as individuals, but for the overall community</li>
        </ul>
        <p className="mt-4">Examples of unacceptable behavior:</p>
        <ul className="mt-2.5">
          <li>The use of sexualized language or imagery, and sexual attention or advances of any kind</li>
          <li>Trolling, insulting or derogatory comments, and personal or political attacks</li>
          <li>Public or private harassment</li>
          <li>
            Publishing others' private information, such as a physical or email address, without
            their explicit permission
          </li>
          <li>Other conduct which could reasonably be considered inappropriate in a professional setting</li>
        </ul>
      </section>

      <section>
        <h2>Enforcement Responsibilities</h2>
        <p>
          The project maintainer is responsible for clarifying and enforcing our standards of
          acceptable behavior and will take appropriate and fair corrective action in response to
          any behavior deemed inappropriate, threatening, offensive, or harmful.
        </p>
      </section>

      <section>
        <h2>Scope</h2>
        <p>
          This Code of Conduct applies within all community spaces (issues, pull requests,
          discussions on this repository), and also when an individual is officially representing
          the community in public spaces.
        </p>
      </section>

      <section>
        <h2>Enforcement</h2>
        <p>
          Instances of abusive, harassing, or otherwise unacceptable behavior may be reported to
          the maintainer at <a href="mailto:josepacelli@gmail.com">josepacelli@gmail.com</a>. All
          complaints will be reviewed and investigated promptly and fairly. The maintainer is
          obligated to respect the privacy and security of the reporter of any incident.
        </p>
      </section>

      <section>
        <h2>Enforcement Guidelines</h2>
        <p>
          <strong>1. Correction</strong> — Use of inappropriate language or other unprofessional
          behavior. Consequence: a private, written warning explaining the violation.
        </p>
        <p className="mt-2.5">
          <strong>2. Warning</strong> — A violation through a single incident or series of actions.
          Consequence: a warning with consequences for continued behavior, including no interaction
          with the people involved for a specified period.
        </p>
        <p className="mt-2.5">
          <strong>3. Temporary Ban</strong> — A serious violation of community standards.
          Consequence: a temporary ban from any interaction or public communication with the
          community.
        </p>
        <p className="mt-2.5">
          <strong>4. Permanent Ban</strong> — A pattern of violation, including harassment of an
          individual or aggression toward classes of individuals. Consequence: a permanent ban
          from any public interaction within the community.
        </p>
      </section>

      <section>
        <h2>Attribution</h2>
        <p>
          Adapted from the{' '}
          <a href="https://www.contributor-covenant.org/version/2/1/code_of_conduct.html">
            Contributor Covenant
          </a>
          , version 2.1. Full text and translations at{' '}
          <a href="https://www.contributor-covenant.org">contributor-covenant.org</a>.
        </p>
      </section>
    </LegalLayout>
  )
}
