(ns edge.phonebook.datadog)

(defn get-tracer
  []
  (when-let [GlobalTracer (resolve 'io.opentracing.util.GlobalTracer)]
    (let [GlobalTracer#get (.getMethod GlobalTracer "get" nil)
          tracer (.invoke GlobalTracer#get GlobalTracer nil)]
      tracer)))

(defn get-active-span
  []
  (when-let [tracer (get-tracer)]
    (. tracer activeSpan)))

(defn trace [f & args]
  (let [tracer (get-tracer)
        DDTags (resolve 'datadog.trace.api.DDTags)]
    (if (and tracer DDTags)
      (let [DDTags#SERVICE_NAME (.get (.getField DDTags "SERVICE_NAME") String)
            span (. (. tracer buildSpan (str f)) start)]
        (. span setTag DDTags#SERVICE_NAME "commsor")
        (try
          (. tracer activateSpan span)
          (apply f args)
          (finally (. span finish))))
      (apply f args))))
