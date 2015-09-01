(ns witan.ui.components.model-diagram
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [thi.ng.geom.svg.core :as svg]
            [sablono.core :as sablono]))

;; All values will used in SVG without units.
(def render-options
  {:box-height        30
   :box-h-spacing     20
   :box-width         125
   :box-w-spacing     75
   :group-box-padding 6
   :canvas-width      600
   :padding           10})

(defn stacked-box
  "Draws the ith box in a stack starting at (x-offset, y-offset), with class cl."
  [render-options x-offset y-offset cl i]
  (let [{:keys [row-height box-height box-width]} render-options
        y-start (+ (* row-height i) y-offset)]
    (svg/rect [x-offset y-start]
              box-width
              box-height
              {:class cl
               :key (str "box-" x-offset "-" i)})))

(defn stack-of-boxes
  "Draws a stack of n-boxes boxes starting at x-offset, with y position determined so as to be centred
  on a stack with max-n boxes in it. Boxes will have class cl."
  [render-options x-offset max-n n-boxes cl]
  (let [{:keys [row-height]} render-options
        y-offset (* (/ row-height 2)
                    (- max-n n-boxes))]
    (mapv (partial stacked-box
                   render-options
                   x-offset
                   y-offset
                   cl)
          (range n-boxes))))

(defn stacked-line
  "Similar to the above, draws the ith line in a stack, starting at y-offset, running from x-start to x-end."
  [render-options x-start x-end y-offset i]
  (let [{:keys [row-height box-height]} render-options
        line-y (+ (* row-height i) (/ box-height 2) y-offset)]
    (svg/line [x-start line-y]
              [x-end line-y]
              {:class "line"
               :key (str "line-" x-start "-" i)})))

(defn stack-of-lines
  "See stack-of-boxes."
  [render-options x-start x-end max-n n-lines]
  (let [{:keys [row-height]} render-options
        y-offset (* (/ row-height 2) (- max-n n-lines))]
    (mapv (partial stacked-line
                   render-options
                   x-start
                   x-end
                   y-offset)
          (range n-lines))))

(defn group-box
  "Draws a box that groups together a number of boxes. Is drawn relative to a stack of boxes starting at
  (x-offset, y-offset) containing boxes from i-start to i-end."
  [render-options x-offset y-offset i-start i-end]
  (let [{:keys [row-height box-width box-h-spacing group-box-padding]} render-options
        x-start (- x-offset group-box-padding)
        y-start (- (+ (* row-height i-start) y-offset) group-box-padding)
        rect-width (+ box-width (* 2 group-box-padding))
        rect-height (+ (* row-height (- i-end i-start))
                       (* -1 box-h-spacing)
                       (* 2 group-box-padding))]
    (svg/rect [x-start y-start]
              rect-width
              rect-height
              {:class "group"
               :key (str "group-box-" i-start)})))

(defn group-ranges
  "Given a list of group sizes, gives the start and end indices of each group i.e.
  [2 2 3] -> ((0 2) (2 4) (4 7)). Used for drawing the output grouping boxes."
  [n-outputs]
  (let [c (reductions + 0 n-outputs)]
    (apply map list [(drop-last c) (rest c)])))

(defn group-boxes
  "Draws a set of group boxes, specified by group-sizes (see group-ranges)."
  [render-options x-offset max-n group-sizes]
  (let [{:keys [row-height]} render-options
        ranges (group-ranges group-sizes)
        n-boxes (apply + group-sizes)]
    (mapv #(group-box
            render-options
            x-offset
            (* (/ row-height 2) (- max-n n-boxes))
            (first %)
            (second %))
          ranges)))

(defn render-model
  "Draws a digram for a given model specification."
  [render-options model]
  (let [{:keys [canvas-width box-width box-w-spacing box-height box-h-spacing padding]} render-options
        {:keys [n-inputs n-outputs]} model
        total-outputs (apply + n-outputs)
        max-n (max n-inputs total-outputs)
        ;; geometry related definitions
        column-width (+ box-width box-w-spacing) ;; the distance from one column to the next
        row-height (+ box-height box-h-spacing) ;; the distance from one row to the next
        output-column-start-x (* 2 column-width) ;; the x-position that the column of output boxes starts
        ;; add the calculated values to render-options to avoid recalculating them everywhere
        render-options-plus (merge render-options {:column-width column-width :row-height row-height })]
    (sablono/html (svg/svg
                    {:width  (+ canvas-width (* 2 padding))
                     :height (+ (* max-n row-height) (* 2 padding))}
                    (svg/group
                      {:transform (str "translate(" padding ", " padding ")")
                       :key "model-diagram-g"}
                      (concat
                        (stack-of-boxes render-options-plus 0 max-n n-inputs "input")
                        (stack-of-lines render-options-plus box-width column-width max-n n-inputs)
                        [(svg/rect [column-width 0]
                                   box-width
                                   (- (* max-n row-height) box-h-spacing)
                                   {:class "model" :key "model-box"})]
                        (group-boxes render-options-plus output-column-start-x max-n n-outputs)
                        (stack-of-boxes render-options-plus output-column-start-x max-n total-outputs "output")
                        (stack-of-lines render-options-plus
                                        (- output-column-start-x box-w-spacing)
                                        output-column-start-x
                                        max-n
                                        total-outputs)))))))

(defcomponent diagram
  [model-shape owner]
  (render [this]
    (render-model render-options model-shape)))
