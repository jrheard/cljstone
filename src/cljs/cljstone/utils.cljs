(ns cljstone.utils)

(def next-id (atom 0))

(defn get-next-id []
  (let [id-to-return @next-id]
    (swap! next-id inc)
    id-to-return))

; props to https://stackoverflow.com/questions/3249334/test-whether-a-list-contains-a-specific-value-in-clojure
(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))
