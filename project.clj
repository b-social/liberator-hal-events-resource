(defproject b-social/liberator-hal-events-resource "0.0.19-SNAPSHOT"
  :description "A HAL events resource for liberator."
  :url "https://github.com/b-social/liberator-hal-events-resource"

  :license {:name "The MIT License"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies [[halboy "5.1.0"]
                 [b-social/liberator-mixin "0.0.57"]
                 [b-social/jason "0.1.5"]
                 [b-social/hype "1.0.0"]]

  :plugins [[lein-cloverage "1.2.3"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.7.0"]
            [lein-changelog "0.3.2"]
            [lein-eftest "0.5.9"]
            [lein-codox "0.10.8"]
            [lein-cljfmt "0.6.4"]
            [lein-kibit "0.1.8"]
            [lein-bikeshed "0.5.2"]]

  :profiles {:shared {:dependencies
                      [[org.clojure/clojure "1.11.1"]
                       [ring/ring-mock "0.4.0"]
                       [clj-time "0.15.2"]
                       [faker "0.3.2"]
                       [eftest "0.5.9"]]}
             :dev    [:shared {:source-paths ["dev"]
                               :eftest       {:multithread? false}}]
             :test   [:shared {:eftest {:multithread? false}}]}

  :cloverage
  {:ns-exclude-regex [#"^user"]}

  :codox
  {:namespaces  [#"^liberator-hal-events-resource\."]
   :output-path "docs"
   :doc-paths   ["docs"]
   :source-uri  "https://github.com/b-social/liberator-hal-events-resource/blob/{version}/{filepath}#L{line}"}

  :cljfmt {:indents ^:replace {#".*" [[:inner 0]]}}

  :deploy-repositories
  {"releases" {:url "https://repo.clojars.org" :creds :gpg}}

  :release-tasks
  [["shell" "git" "diff" "--exit-code"]
   ["change" "version" "leiningen.release/bump-version" "release"]
   ["codox"]
   ["changelog" "release"]
   ["shell" "sed" "-E" "-i" "" "s/\"[0-9]+\\.[0-9]+\\.[0-9]+\"/\"${:version}\"/g" "README.md"]
   ["shell" "git" "add" "."]
   ["vcs" "commit"]
   ["vcs" "tag"]
   ["deploy"]
   ["change" "version" "leiningen.release/bump-version"]
   ["vcs" "commit"]
   ["vcs" "tag"]
   ["vcs" "push"]]

  :aliases {"test"      ["with-profile" "test" "eftest" ":all"]
            "precommit" ["do"
                         ["check"]
                         ["kibit" "--replace"]
                         ["cljfmt" "fix"]
                         ["with-profile" "test" "bikeshed"
                          "--name-collisions" "false"
                          "--verbose" "true"]
                         ["test"]]})
