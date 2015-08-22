(ns cljstone.app)

(defn get-nodes-by-selector [selector]
  (.querySelectorAll js/document selector))

(defn set-text! [node content]
  (set! (. node -textContent) content))

(def p1 (aget (get-nodes-by-selector "#board-half-1") 0))
(def p2 (aget (get-nodes-by-selector "#board-half-2") 0))

(set-text! p1 "sup")
(set-text! p2 "yo")
