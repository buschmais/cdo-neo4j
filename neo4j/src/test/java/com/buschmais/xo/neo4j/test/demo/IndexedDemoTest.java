package com.buschmais.xo.neo4j.test.demo;

import java.net.URISyntaxException;
import java.util.Collection;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.buschmais.xo.api.XOManager;
import com.buschmais.xo.api.bootstrap.XOUnit;
import com.buschmais.xo.neo4j.test.AbstractNeo4jXOManagerTest;
import com.buschmais.xo.neo4j.test.demo.composite.Group;
import com.buschmais.xo.neo4j.test.demo.composite.Person;

@RunWith(Parameterized.class)
public class IndexedDemoTest extends AbstractNeo4jXOManagerTest {

    public IndexedDemoTest(XOUnit xoUnit) {
        super(xoUnit);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> getXOUnits() throws URISyntaxException {
        return xoUnits(Group.class, Person.class);
    }

    @Test
    public void test() {
        XOManager xoManager = getXoManager();
        xoManager.currentTransaction().begin();
        Person person1 = xoManager.create(Person.class);
        person1.setName("Peter");
        xoManager.currentTransaction().commit();
        xoManager.currentTransaction().begin();
        Person person2 = xoManager.find(Person.class, "Peter").getSingleResult();
        Assert.assertThat(person2, Matchers.equalTo(person1));
        xoManager.currentTransaction().commit();
    }
}
