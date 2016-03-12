# VelocityTemplateMediator
Synapse Mediator for creating properties out of templates

Sample Syntax
```xml
<velocityTemplate media-type="xml|json">
   <format>
      <person>
         <name>$name</name>
         <age>$age</age>
      </person>
   </format>
   <args>
      <arg name="name" expression="$ctx:name" />
      <arg name="age" expression="$ctx:age" />
   </args>
   <target target-type="property|body|custom|header" name="propertyName" property-type="string|om" scope="synapse|axis2|operation" />
</velocityTemplate>
```

XML segment under the format tag will be replaced with corresponding values in args. And it will create OM type property in scope with the given name.


Supports iterations through arrays


```xml
 <velocityTemplate media-type="xml" xmlns="http://ws.apache.org/ns/synapse">
                <format>
                    <person xmlns="">
                        <name>$name</name>
                        <age>$age</age>
                        <test>$list</test>
                        <?xml-multiple?>
                        #foreach($item in $list)
                            <a>$item.getText()</a>
                        #end
                    </person>
                </format>
                <args>
                    <arg name="name" expression="$ctx:name" />
                    <arg name="age" expression="$ctx:age" />
                    <arg name="list" expression="//subject" />
                </args>
               <target target-type="property" name="testProp" scope="synapse" property-type="om"/>
</velocityTemplate>

```
