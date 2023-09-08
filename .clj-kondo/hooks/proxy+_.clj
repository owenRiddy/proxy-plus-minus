(ns hooks.proxy+-
  (:require [clj-kondo.hooks-api :as api]))

(defn proxy+-
  [{:keys [node]}]
  (let [unnamed?
        (-> node :children second api/token-node? not)

        _superclass-args
        (if unnamed?
          (second (:children node))
          (nth (:children node) 2))

        reify-like
        (if unnamed?
          (-> node :children (nthrest 2))
          (-> node :children (nthrest 3)))
        new-node (api/list-node
                  (list*
                   (api/token-node 'reify)
                   reify-like))]
    {:node new-node}))
