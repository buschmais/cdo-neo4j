package com.buschmais.xo.neo4j.embedded.test.query;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.embedded.test.AbstractNeo4jXOManagerTest;
import com.buschmais.xo.neo4j.embedded.test.Neo4jDatabase;
import com.buschmais.xo.neo4j.embedded.test.query.composite.A;
import com.buschmais.xo.neo4j.embedded.test.query.composite.A2B;
import com.buschmais.xo.neo4j.embedded.test.query.composite.B;

@RunWith(Parameterized.class)
public class PathTest extends AbstractNeo4jXOManagerTest {

    public PathTest(XOUnit xoUnit) {
        super(xoUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getXOUnits() throws URISyntaxException {
        return xoUnits(asList(Neo4jDatabase.MEMORY), asList(A.class, B.class, A2B.class));
    }

    @Test
    public void queryReturningPath() {
        XOManager xoManager = getXoManager();
        xoManager.currentTransaction().begin();
        A a = xoManager.create(A.class);
        B b = xoManager.create(B.class);
        A2B a2b = xoManager.create(a, A2B.class, b);
        xoManager.currentTransaction().commit();
        xoManager.currentTransaction().begin();
        Result<CompositeRowObject> result = xoManager.createQuery("match path=(a:A)-->(b:B) return path").execute();
        List<?> path = result.getSingleResult().get("path", List.class);
        assertThat(path.size(), equalTo(3));
        assertThat(path.get(0), Matchers.<Object>equalTo(a));
        assertThat(path.get(1), Matchers.<Object>equalTo(a2b));
        assertThat(path.get(2), Matchers.<Object>equalTo(b));
        xoManager.currentTransaction().commit();
    }

}
