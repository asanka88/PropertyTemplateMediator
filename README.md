# PropertyTemplateMediator
Synapse Mediator for creating properties out of templates

Sample Syntax
```xml
<propertyTemplate name="outProp" scope="default">
<format>
  <person>
  	<name>$name</name>
  	<age>$age</age>
  </person>
</format>
<args>
	<arg name="name" expression="$ctx:name"/>
<arg name="age" expression="$ctx:age"/>
</args>
</propertyTemplate>
```

XML segment under the format tag will be replaced with corresponding values in args. And it will create OM type property in scope with the given name.