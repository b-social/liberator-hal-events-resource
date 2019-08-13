(defproject b-social/liberator-hal-events-resource "0.0.3"
  :description "FIXME: write description"
  :url "https://github.com/b-social/liberator-hal-events-resource"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.8.1"]
                 [liberator "0.15.2"]
                 [halboy "4.0.1"]
                 [camel-snake-kebab "0.4.0"]
                 [clj-time "0.15.1"]
                 [bidi "2.1.4"]
                 [faker "0.3.2"]
                 [b-social/liberator-mixin "0.0.11"]]
  :plugins [[lein-cloverage "1.0.13"]
            [lein-shell "0.5.0"]
            [lein-ancient "0.6.15"]
            [lein-changelog "0.3.2"]]
  :profiles {:shared {:dependencies [[org.clojure/clojure "1.10.0"]
                                     [ring/ring-mock "0.3.2"]
                                     [eftest "0.5.3"]]}
             :dev    [:shared]
             :test   [:shared]}
  :eftest {:multithread? false}
  :deploy-repositories {"releases" {:url   "https://repo.clojars.org"
                                    :creds :gpg}}
  :aliases {"update-readme-version" ["shell" "sed" "-i" "s/\\\\[b-social\\\\/liberator-hal-events-resource \"[0-9.]*\"\\\\]/[b-social\\\\/liberator-hal-events-resource \"${:version}\"]/" "README.md"]
            "test"                  ["eftest" ":all"]}
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]])
