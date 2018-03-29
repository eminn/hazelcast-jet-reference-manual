import com.hazelcast.jet.config.EdgeConfig;
import com.hazelcast.jet.core.DAG;
import com.hazelcast.jet.core.Partitioner;
import com.hazelcast.jet.core.Vertex;
import com.hazelcast.jet.core.processor.SinkProcessors;
import com.hazelcast.jet.core.processor.SourceProcessors;

import static com.hazelcast.jet.Util.entry;
import static com.hazelcast.jet.core.Edge.between;
import static com.hazelcast.jet.core.Edge.from;
import static com.hazelcast.jet.core.Partitioner.HASH_CODE;
import static com.hazelcast.jet.core.processor.Processors.mapP;
import static com.hazelcast.jet.function.DistributedFunctions.entryKey;
import static com.hazelcast.jet.function.DistributedFunctions.wholeItem;
import static java.nio.charset.StandardCharsets.UTF_8;

class ExpertZoneDag {

    private static Vertex
            input,
            output,
            tokenize,
            accumulate,
            combine,
            stage1,
            stage2,
            stopwordSource,
            tickerSource,
            generateTrades;

    private static
    //tag::s1[]
    DAG dag = new DAG();
    //end::s1[]


    static void s2() {
        //tag::s2[]
        // <1>
        Vertex source = dag.newVertex("source",
                SourceProcessors.readFilesP(".", UTF_8, "*", (file, line) -> line)
        );
        Vertex transform = dag.newVertex("transform", mapP(
                (String line) -> entry(line, line.length())
        ));
        Vertex sink = dag.newVertex("sink", SinkProcessors.writeMapP("sinkMap"));

        // <2>
        source.localParallelism(1);

        // <3>
        dag.edge(between(source, transform));
        dag.edge(between(transform, sink));
        //end::s2[]
    }

    static void s3() {
        //tag::s3[]
        Vertex source = dag.newVertex("source",
                SourceProcessors.readFilesP(".", UTF_8, "*", (file, line) -> line)
        );
        Vertex toUpper = dag.newVertex("toUpper", mapP((String in) -> in.toUpperCase()));
        Vertex toLower = dag.newVertex("toLower", mapP((String in) -> in.toLowerCase()));

        dag.edge(between(source, toUpper));
        dag.edge(from(source, 1).to(toLower));
        //end::s3[]
    }

    static void s4() {
        //tag::s4[]
        dag.edge(between(tokenize, accumulate)
                   .partitioned(wholeItem(), HASH_CODE))
           .edge(between(accumulate, combine)
                   .distributed()
                   .partitioned(entryKey()));
        //end::s4[]
    }

    static void s5() {
        //tag::s5[]
        dag.edge(between(input, output));
        //end::s5[]
    }

    static void s6() {
        //tag::s6[]
        dag.edge(between(input, output).isolated());
        //end::s6[]
    }

    static void s7() {
        //tag::s7[]
        dag.edge(between(input, output).broadcast());
        //end::s7[]
    }

    static void s8() {
        //tag::s8[]
        dag.edge(between(tokenize, accumulate)
                .partitioned(wholeItem(), Partitioner.HASH_CODE))
           .edge(between(accumulate, combine)
                   .distributed()
                   .partitioned(entryKey()));
        //end::s8[]
    }

    static void s9() {
        //tag::s9[]
        dag.edge(between(stage1, stage2).distributed().allToOne());
        //end::s9[]
    }

    static void s10() {
        //tag::s10[]
        dag.edge(between(stopwordSource, tokenize).broadcast().priority(-1));
        //end::s10[]
    }

    static void s11() {
        //tag::s11[]
        dag.edge(between(tickerSource, generateTrades)
                .setConfig(new EdgeConfig().setQueueSize(512)));
        //end::s11[]
    }
}