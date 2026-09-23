import { BackToTopButton } from '@/components/layout/BackToTopButton'
import { Footer } from '@/components/layout/Footer'
import { Header } from '@/components/layout/Header'
import { Compare } from '@/components/sections/Compare'
import { Contribute } from '@/components/sections/Contribute'
import { Faq } from '@/components/sections/Faq'
import { Features } from '@/components/sections/Features'
import { Hero } from '@/components/sections/Hero'
import { HowItWorks } from '@/components/sections/HowItWorks'
import { Install } from '@/components/sections/Install'
import { Screenshots } from '@/components/sections/Screenshots'
import { Support } from '@/components/sections/Support'

function App() {
  return (
    <div className="min-h-svh w-full">
      <Header />
      <div className="mx-auto max-w-[920px] px-6">
        <Hero />
        <HowItWorks />
        <Screenshots />
        <Install />
        <Features />
        <Compare />
        <Faq />
        <Contribute />
        <Support />
      </div>
      <Footer />
      <BackToTopButton />
    </div>
  )
}

export default App
