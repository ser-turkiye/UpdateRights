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

        String ids ="SD07PRJ_DOC244ede255c-341c-438d-8380-b61702db32f7182024-01-02T14:13:15.415Z011"


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
