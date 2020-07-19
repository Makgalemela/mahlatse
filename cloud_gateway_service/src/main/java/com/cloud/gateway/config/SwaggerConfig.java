package com.cloud.gateway.config;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import io.swagger.annotations.Api;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Primary
public class SwaggerConfig implements SwaggerResourcesProvider{

	@Autowired
	RouteLocator routeLocator;
	
	@Bean
	public Docket api() {
		ParameterBuilder aParameterBuilder = new ParameterBuilder();
		aParameterBuilder.name("Authorization").modelRef(new ModelRef("string")).parameterType("header").required(false)
				.build();
		List<Parameter> aParameters = new ArrayList<>(1);
		aParameters.add(aParameterBuilder.build());
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.withClassAnnotation(Api.class)).paths(PathSelectors.any()).build()
				.pathMapping("/").apiInfo(apiInfo()).useDefaultResponseMessages(false)
				.globalOperationParameters(aParameters);

	}

	public ApiInfo apiInfo() {
	final ApiInfoBuilder builder = new ApiInfoBuilder();
	builder.title("Cloud Swagger").version("1.0").license("(C) Copyright cloud ")
			.description("The API provides a platform to query build  cloud api")
			.contact(new Contact(" cloud ", "cloud", "support@cloud.com"));
	return builder.build();
}
	@Override
	public List<SwaggerResource> get() {
		//Dynamic introduction of micro services using routeLocator
		List<SwaggerResource> resources = new ArrayList<>();
		resources.add(swaggerResource("zuul","/v2/api-docs","1.0"));
		routeLocator.getRoutes().forEach(route ->{
			resources.add(swaggerResource(route.getId(),route.getFullPath().replace("**", "v2/api-docs"), "2.0"));
		});
		return resources;
	}
	
	private SwaggerResource swaggerResource(String name,String location, String version) {
		SwaggerResource swaggerResource = new SwaggerResource();
		swaggerResource.setName(name);
		swaggerResource.setLocation(location);
		swaggerResource.setSwaggerVersion(version);
		return swaggerResource;
	}
	
}
