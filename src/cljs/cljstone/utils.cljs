(ns cljstone.utils)

; props to https://stackoverflow.com/questions/3249334/test-whether-a-list-contains-a-specific-value-in-clojure
(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))
