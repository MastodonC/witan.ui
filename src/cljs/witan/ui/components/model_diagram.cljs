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
   :box-w-spacing     80
   :group-box-padding 7
   :canvas-width      600
   :padding           40
   :top-offset        60
   :label-offset      0
   :label-size        20
   ;; this must be less than :padding to avoid clipping
   :highlight-padding 40})

(defn stacked-box
  "Draws the ith box in a stack starting at (x-offset, y-offset), with class cl."
  [render-options x-offset y-offset cl i]
  (let [{:keys [row-height box-height box-width]} render-options
        y-start (+ (* row-height i) y-offset)]
    (svg/rect [x-offset y-start]
              box-width
              box-height
              {:class cl
               :key   (str "box-" x-offset "-" i)})))

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
               :key   (str "line-" x-start "-" i)})))

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
               :rx    7
               :ry    7
               :key   (str "group-box-" i-start)})))

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

(defn stage-highlight
  "Draws a highlight box which indicates which stage of the modelling process is being configured.
  The fill is set according to whether this box is `highlighted`."
  [render-options max-n stage-index highlighted]
  (let [{:keys [row-height column-width box-width box-h-spacing highlight-padding top-offset]} render-options
        x-start (+ (* -1 highlight-padding) (* stage-index column-width))
        y-start (* -1 highlight-padding)
        x-size (+ box-width (* 2 highlight-padding))
        y-size (+ (* row-height max-n) (* 2 highlight-padding) (* -1 box-h-spacing) top-offset)
        fill-colour (if highlighted "#dddddd" "white")]
    (svg/rect [x-start y-start] x-size y-size
              {:class "highlight"
               :key   (str "highlight-box-" stage-index)
               :fill  fill-colour})))

(defn stage-highlights
  "Draws a highlight box for each stage. The highlighted box will have a different fill than the others."
  [render-options max-n highlight-index]
  (mapv #(stage-highlight render-options max-n % (= % highlight-index)) (range 3)))

(defn stage-label
  [render-options stage-index highlighted]
  (let [{:keys [box-width column-width label-offset label-size]} render-options
        x-center (+ (* stage-index column-width) (/ box-width 2))
        stroke-colour (if highlighted "#555555" "#dddddd")
        text-colour (if highlighted "black" "#555555")]
    (svg/group {:key (str "label-group-" stage-index)}
               (svg/circle [x-center label-offset] label-size {:class  "forecast-label-circle"
                                                               :stroke stroke-colour
                                                               :key    "label-circle"})
               (svg/text [x-center label-offset]
                         (str (+ 1 stage-index))
                         {:class       "forecast-label-text"
                          :fill        text-colour
                          :text-anchor "middle"
                          :style       {:dominant-baseline "middle"}
                          :key         "label-text"}))))

(defn stage-labels
  [render-options highlight-index]
  (mapv #(stage-label render-options % (= % highlight-index)) (range 3)))

(defn stage-to-index
  [stage]
  (case stage
    :input 0
    :model 1
    :output 2))

(defn render-model
  "Draws a digram for a given model specification."
  [render-options model-conf]
  (let [{:keys [canvas-width box-width box-w-spacing box-height box-h-spacing padding top-offset]} render-options
        {:keys [n-inputs n-outputs action]} model-conf
        total-outputs (apply + n-outputs)
        max-n (max n-inputs total-outputs)
        ;; geometry related definitions
        column-width (+ box-width box-w-spacing)            ;; the distance from one column to the next
        row-height (+ box-height box-h-spacing)             ;; the distance from one row to the next
        output-column-start-x (* 2 column-width)            ;; the x-position that the column of output boxes starts
        ;; add the calculated values to render-options to avoid recalculating them everywhere
        render-options-plus (merge render-options {:column-width column-width :row-height row-height})
        stage-index (stage-to-index action)]
    (sablono/html (svg/svg
                    {:width  (+ canvas-width (* 2 padding))
                     :height (+ (* max-n row-height) (* 2 padding) top-offset)
                     :key    "model-diagram-g1"}
                    (svg/group
                      {:transform (str "translate(" padding ", " padding ")")
                       :key       "model-diagram-g2"}
                      (svg/group
                        {:key "model-diagram-g3"}
                        (concat
                          ;; stage highlight
                          (stage-highlights render-options-plus max-n stage-index)
                          ;; stage labels
                          (stage-labels render-options-plus stage-index)))
                      (svg/group
                        {:transform (str "translate(0," top-offset ")")
                         :key       "model-diagram-g4"}
                        (concat
                          ;; inputs
                          (stack-of-boxes render-options-plus 0 max-n n-inputs "input")
                          ;; lines from inputs to model
                          (stack-of-lines render-options-plus box-width column-width max-n n-inputs)
                          ;; model
                          [(svg/rect [column-width 0]
                                     box-width
                                     (- (* max-n row-height) box-h-spacing)
                                     {:class "model" :key "model-box"})]
                          ;; output grouping boxes
                          (group-boxes render-options-plus output-column-start-x max-n n-outputs)
                          ;; outputs
                          (stack-of-boxes render-options-plus output-column-start-x max-n total-outputs "output")
                          ;; lines from model to outputs
                          (stack-of-lines render-options-plus
                                          (- output-column-start-x box-w-spacing)
                                          output-column-start-x
                                          max-n
                                          total-outputs)
                          )))))))

(defcomponent diagram
  [model-conf owner]
  (render [this]
    (render-model render-options model-conf)))
