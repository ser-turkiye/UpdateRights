package junit

import de.ser.doxis4.agentserver.AgentExecutionResult
import org.junit.*
import ser.UpdateRights

class ExampleTests {

    Binding binding

    @BeforeClass
    static void initSessionPool() {
        AgentTester.initSessionPool()
    }

    @Before
    void retrieveBinding() {
        binding = AgentTester.retrieveBinding()
    }

    @Test
    void testForAgentResult() {
        def agent = new UpdateRights();

        String ids ="SD07PRJ_DOC2458d7a521-8d88-4d14-a0bb-98b3b7a05372182023-12-25T08:53:35.673Z011"


        binding["AGENT_EVENT_OBJECT_CLIENT_ID"] = ids

        def result = (AgentExecutionResult)agent.execute(binding.variables)
        assert result.resultCode == 0
    }

    /*
        def agent = new GroovyAgent()
        binding["AGENT_EVENT_OBJECT_CLIENT_ID"] = "SD0bGENERIC_DOC24d79641cb-c437-4e39-81af-3051824baa25182021-01-14T10:09:04.857Z011"
        def result = (AgentExecutionResult)agent.execute(binding.variables)
        assert result.resultCode == 0
        assert result.executionMessage.contains("Linux")
        assert agent.eventInfObj instanceof IDocument
     */


    @Test
    void testForJavaAgentMethod() {
        //def agent = new JavaAgent()
        //agent.initializeGroovyBlueline(binding.variables)
        //assert agent.getServerVersion().contains("Linux")
    }

    @After
    void releaseBinding() {
        AgentTester.releaseBinding(binding)
    }

    @AfterClass
    static void closeSessionPool() {
        AgentTester.closeSessionPool()
    }
}
