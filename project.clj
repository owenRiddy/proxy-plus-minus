(defproject com.rpl/proxy-plus "0.0.11-SNAPSHOT"
  :description "A faster and more usable replacement for Clojure's proxy."
  :java-source-paths ["test/java"]
  :test-paths ["test/clj"]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.ow2.asm/asm "4.2"]
                 [org.ow2.asm/asm-analysis "4.2"]
                 [org.ow2.asm/asm-commons "4.2"]
                 [org.ow2.asm/asm-util "4.2"]]

  :repositories
  {"nexus-releases"
   {:url
    "https://nexus.redplanetlabs.com/repository/maven-public-releases"}}

  :profiles {:bench {:dependencies [[criterium "0.4.5"]]}})
