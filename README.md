[![Build Status](https://travis-ci.org/dhatim/dropwizard-jwt-cookie-authentication.svg?branch=master)](https://travis-ci.org/dhatim/dropwizard-jwt-cookie-authentication)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.dhatim/dropwizard-jwt-cookie-authentication/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.dhatim/dropwizard-jwt-cookie-authentication)
[![Coverage Status](https://coveralls.io/repos/github/dhatim/dropwizard-jwt-cookie-authentication/badge.svg?branch=master)](https://coveralls.io/github/dhatim/dropwizard-jwt-cookie-authentication?branch=master)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/org.dhatim/dropwizard-jwt-cookie-authentication/badge.svg)](http://www.javadoc.io/doc/org.dhatim/dropwizard-jwt-cookie-authentication)

# dropwizard-jwt-cookie-authentication

Statelessness is not only an architectural constaint of RESTful applications, it also comes with a lot of advantages regarding scalability and memory usage.

A common pattern is to provide the client with a signed JWT containing all necessary authorization and/or session state information. This JWT must then be passed along subsequent requests, usually in bearer Authorization HTTP headers.

However, in the particular case where clients of the RESTful application are web applications, it is much more interesting to use cookies. The browser will automatically read, store, send and expire the tokens, saving front-end developers the hassle of doing it themselves.

This dropwizard bundle makes things simple for back-end developpers too. It automatically serializes/deserializes session information into/from JWT cookies.

## Enabling the bundle

### Add the dropwizard-jwt-cookie-authentication dependency

Add the dropwizard-jwt-cookie-authentication library as a dependency to your `pom.xml` file:

```xml
<dependency>
    <groupId>org.dhatim</groupId>
    <artifactId>dropwizard-jwt-cookie-authentication</artifactId>
    <version>2.0.0</version>
</dependency>
  ```

### Edit you app's Dropwizard YAML config file

The default values are shown below. If they suit you, this step is optional.

```yml
jwtCookieAuth:
  secretSeed: null
  httpsOnlyCookie: false
  sessionExpiryVolatile: PT30m
  sessionExpiryPersistent: P7d
```

### Add the 'JwtCookieAuthConfiguration' to your application configuration class:

This step is also optional if you skipped the previous one.

```java
@Valid
@NotNull
private JwtCookieAuthConfiguration jwtCookieAuth = new JwtCookieAuthConfiguration();

public JwtCookieAuthConfiguration getJwtCookieAuth() {
  return jwtCookieAuth;
}
```

### Add the bundle to the dropwizard application

```java
public void initialize(Bootstrap<MyApplicationConfiguration> bootstrap) {
  bootstrap.addBundle(JwtCookieAuthBundle.getDefault());
}
```

If you have a custom configuration fot the bundle, specify it like so:
```java
bootstrap.addBundle(JwtCookieAuthBundle.getDefault().withConfigurationSupplier(MyAppConfiguration::getJwtCookieAuth));
```

## Using the bundle

By default, the JWT cookie is serialized from / deserialized in an instance of `DefaultJwtCookiePrincipal`.

When the user authenticate, you must put an instance of `DefaultJwtCookiePrincipal` in the security context (which you can inject in your resources using the `@Context` annotation) using `JwtCookiePrincipal.addInContext`
```java
JwtCookiePrincipal principal = new DefaultJwtCookiePrincipal(name);
principal.addInContext(context);
```

Once a principal has been set, it can be retrieved using the `@Auth` annotation in method signatures.

Each time an API endpoint is called, a fresh cookie JWT is issued to reset the session TTL. You can use the `@DontRefreshSession` on methods where this behavior is unwanted.

To specify a max age in the cookie (aka "remember me"), use `DefaultJwtCookiePrincipal.setPresistent(true)`.

It is a stateless auhtentication method, so there is no real way to invalidate a session other than waiting for the JWT to expire. However calling `JwtCookiePrincipal.removeFromContext(context)` will make browsers discard the cookie by setting the cookie expiration to a past date.

Principal roles can be specified via the `DefaultJwtCookiePrincipal.setRoles(...)` method. You can then define fine grained access control using annotations such as `@RolesAllowed` or `@PermitAll`.

Additional custom data can be stored in the Principal using `DefaultJwtCookiePrincipal.getClaims().put(key, value)`.

## Sample application resource
```java
@POST
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public DefaultJwtCookiePrincipal login(@Context ContainerRequestContext requestContext, String name){
    DefaultJwtCookiePrincipal principal = new DefaultJwtCookiePrincipal(name);
    principal.addInContext(requestContext);
    return principal;
}

@GET
public DefaultJwtCookiePrincipal logout(@Context ContainerRequestContext requestContext){
    JwtCookiePrincipal.removeFromContext(requestContext);
}

@GET
@Produces(MediaType.APPLICATION_JSON)
public Subject getPrincipal(@Auth DefaultJwtCookiePrincipal principal){
    return subject;
}

@GET
@Path("idempotent")
@Produces(MediaType.APPLICATION_JSON)
@DontRefreshSession
public DefaultJwtCookiePrincipal getSubjectWithoutRefreshingSession(@Auth DefaultJwtCookiePrincipal principal){
    return principal;
}

@GET
@Path("restricted")
@RolesAllowed("admin")
public String getRestrisctedResource(){
    return "SuperSecretStuff";
}
```

## Custom principal implementation

If you want to use your own Principal class instead of the `DefaultJwtCookiePrincipal`, simply implement the interface `JwtCookiePrincipal` and pass it to the bundle constructor along with functions to serialize it into / deserialize it from JWT claims.

e.g:

```java
bootstrap.addBundle(new JwtCookieAuthBundle<>(MyCustomPrincipal.class, MyCustomPrincipal::toClaims, MyCustomPrincipal::new));
```

## JWT Signing Key

By default, the signing key is randomly generated on application startup. It means that users will have to re-authenticate after each server reboot.

To avoid this, you can specify a `secretSeed` in the configuration. This seed will be used to generate the signing key, which will therefore be the same at each application startup.

Alternatively you can specify your own key factory:
```java
bootstrap.addBundle(new JwtCookieAuthBundle<>(MyApplicationConfiguration::getJwtCookieAuth).setKeyFactory((configuration, environment) -> {/*return your own key*/}));
```

## Javadoc

It's [here](http://dhatim.github.io/dropwizard-jwt-cookie-authentication).
