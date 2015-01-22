import com.thinkaurelius.titan.core.*;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import com.thinkaurelius.titan.core.util.TitanCleanup;
import com.tinkerpop.blueprints.Vertex;

/**
 * @author Daniel Kuppitz (daniel at thinkaurelius.com)
 */
public class Sandbox {

    public static void main(final String[] args) throws InterruptedException {

        TitanGraph g = getGraph();
        g.shutdown();
        TitanCleanup.clear(g);
        g = getGraph();
        createSchema(g);

        final TitanVertex daniel = g.addVertexWithLabel("person");
        final TitanVertex simon = g.addVertexWithLabel("person");

        daniel.setProperty("name", "simon");
        simon.setProperty("name", "simon");
        daniel.addEdge("knows", simon).setProperty("value", 0L);

        g.commit();

        final TitanEdge edge = (TitanEdge) g.getEdges().iterator().next();
        edge.setProperty("value", 1L);

        g.commit();
    }

    private static TitanGraph getGraph() {
        return TitanFactory.build()
                .set("storage.backend", "cassandrathrift")
                .set("storage.hostname", "127.0.0.1")
                .set("index.search.backend", "elasticsearch")
                .set("index.search.directory", "127.0.0.1")
                .set("index.search.elasticsearch.client-only", true)
                .set("index.search.elasticsearch.local-mode", false)
                .set("storage.meta.edgestore.timestamps", true)
                .open();
    }

    private static void createSchema(final TitanGraph graph) {

        final TitanManagement m = graph.getManagementSystem();
        final VertexLabel person;

        if (!m.containsVertexLabel("person")) {
            person = m.makeVertexLabel("person").make();
        } else {
            person = m.getVertexLabel("person");
        }

        if (!m.containsPropertyKey("name")) {
            final PropertyKey name = m.makePropertyKey("name").dataType(String.class).make();
            m.buildIndex("byName", Vertex.class).addKey(name).indexOnly(person).buildMixedIndex("search");
        }

        if (!m.containsPropertyKey("val")) {
            m.makePropertyKey("val").dataType(Long.class).make();
        }

        if (!m.containsEdgeLabel("knows")) {
            m.makeEdgeLabel("knows").make();
        }

        m.commit();
    }
}
