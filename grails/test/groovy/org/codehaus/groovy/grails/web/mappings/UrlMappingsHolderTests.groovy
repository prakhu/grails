package org.codehaus.groovy.grails.web.mapping

import org.codehaus.groovy.grails.validation.ConstrainedProperty;
import org.springframework.core.io.*
import org.codehaus.groovy.grails.web.servlet.mvc.*


class UrlMappingsHolderTests extends AbstractGrailsControllerTests {

    def mappingScript = '''
mappings {
  "/blog/$entry/$year?/$month?/$day?" {
        controller = "blog"
        action = "show"
  }
  "/book/$author/$title/$test" {
      controller = "book"
      action = { "${params.test}" }
  }
  "/$controller/$action?/$id?" {
      controller = { params.controller }
      action = { params.action }
      id = { params.id }
  }
}
'''

   def mappingScript2 = '''
mappings  {
    "/specific/$action?" {
        controller = "someController"
    }

    "/admin/$controller/$action?/$id?" {
        constraints {
            id(matches:/\\d+/)
        }
    }
}
   '''

   def mappingWithNamedArgs = '''
mappings {
    "/author/$lastName/$firstName" (controller:'product', action:'show')
}
'''
   def mappingWithNamedArgsAndClosure = '''
mappings {
    "/author/$lastName/$firstName" (controller:'product') {
         action = 'show'
    }
}
'''

	void testGetReverseMappingWithNamedArgsAndClosure() {
       runTest {
           def res = new ByteArrayResource(mappingWithNamedArgsAndClosure.bytes)

           def evaluator = new DefaultUrlMappingEvaluator()
           def mappings = evaluator.evaluateMappings(res)

           def holder = new DefaultUrlMappingsHolder(mappings)

           def params = [lastName:'Winter', firstName:'Johnny']
           def m = holder.getReverseMapping("product", "show", params)
           assertNotNull "getReverseMapping returned null", m
           
           assertEquals "/author/Winter/Johnny", m.createURL(params)
           
      }
                 
    }

	void testGetReverseMappingWithNamedArgs() {
       runTest {
           def res = new ByteArrayResource(mappingWithNamedArgs.bytes)

           def evaluator = new DefaultUrlMappingEvaluator()
           def mappings = evaluator.evaluateMappings(res)

           def holder = new DefaultUrlMappingsHolder(mappings)

           def params = [lastName:'Winter', firstName:'Johnny']
           def m = holder.getReverseMapping("product", "show", params)
           assertNotNull "getReverseMapping returned null", m
           
           assertEquals "/author/Winter/Johnny", m.createURL(params)
      }
                 
    }
    /**
      TODO: This test is currently failing, whether we actually need the ability to specify excess arguments
      that aren't part of the URL mapping definition is up for debate.
      
     void testGetReverseMappingWithExcessArgs() {
        runTest {
             def res = new ByteArrayResource(mappingScript.bytes)

             def evaluator = new DefaultUrlMappingEvaluator()
             def mappings = evaluator.evaluateMappings(res)

             def holder = new DefaultUrlMappingsHolder(mappings)

             // test with exact argument match
             def m = holder.getReverseMapping("blog", "show", [entry:"foo", year:2007, month:3, day:17, some:"other"])

             assert m
             assertEquals "blog", m.controllerName
             assertEquals "show", m.actionName

        }

    }*/

    void testGetReverseMappingWithVariables() {
             def res = new ByteArrayResource(mappingScript2.bytes)
             def evaluator = new DefaultUrlMappingEvaluator()
             def mappings = evaluator.evaluateMappings(res)

             def holder = new DefaultUrlMappingsHolder(mappings)

            // test with fewer arguments
            def m = holder.getReverseMapping("test", "list",null)
            assert m

            assertEquals "/admin/test/list/1", m.createURL(controller:"test", action:"list",id:1)

            assertEquals "/admin/test/list/1?foo=bar", m.createURL(controller:"test", action:"list",id:1, foo:"bar")

            m = holder.getReverseMapping("someController", "test", null)
            assert m
            assertEquals "/specific/test", m.createURL(controller:"someController", action:"test")
    }

    void testGetReverseMappingWithFewerArgs() {
        runTest {
             def res = new ByteArrayResource(mappingScript.bytes)

             def evaluator = new DefaultUrlMappingEvaluator()
             def mappings = evaluator.evaluateMappings(res)

             def holder = new DefaultUrlMappingsHolder(mappings)

            // test with fewer arguments
            def m = holder.getReverseMapping("blog", "show", [entry:"foo", year:2007])

             assert m
             assertEquals "blog", m.controllerName
             assertEquals "show", m.actionName

        }
    }

    void testGetReverseMappingWithExactArgs() {
        runTest {
             def res = new ByteArrayResource(mappingScript.bytes)

             def evaluator = new DefaultUrlMappingEvaluator()
             def mappings = evaluator.evaluateMappings(res)

             def holder = new DefaultUrlMappingsHolder(mappings)

             // test with exact argument match
             def m = holder.getReverseMapping("blog", "show", [entry:"foo", year:2007, month:3, day:17])

             assert m
             assertEquals "blog", m.controllerName
             assertEquals "show", m.actionName
             assertEquals("/blog/foo/2007/3/17?test=test", m.createURL([controller:"blog",action:"show",entry:"foo",year:2007,month:3,day:17,test:'test']))

            // test with fewer arguments
            m = holder.getReverseMapping("blog", "show", [entry:"foo", year:2007])

             assert m
             assertEquals "blog", m.controllerName
             assertEquals "show", m.actionName



            m = holder.getReverseMapping("book", null, [author:"dierk", title:"GINA", test:3])
            assert m
             assertEquals "book", m.controllerName
             


            m = holder.getReverseMapping(null, null, [:])
            assert m

        }

    }



}

