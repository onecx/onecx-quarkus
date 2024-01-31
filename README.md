# onecx-quarkus

OneCx Quarkus Extensions. Set of Quarkus extensions and libraries to speed up OneCx development of backend microservices.

[![License](https://img.shields.io/github/license/onecx/onecx-quarkus?style=for-the-badge&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0)
[![Release version](https://img.shields.io/maven-central/v/org.tkit.onecx.quarkus/onecx-quarkus-bom?logo=apache-maven&style=for-the-badge&label=Release)](https://search.maven.org/artifact/org.tkit.onecx.quarkus/onecx-quarkus-bom)
[![Quarkus version](https://img.shields.io/maven-central/v/io.quarkus/quarkus-bom?logo=apache-maven&style=for-the-badge&label=Quarkus)](https://search.maven.org/artifact/io.quarkus/quarkus-bom)
[![Supported JVM Versions](https://img.shields.io/badge/JVM-17-brightgreen.svg?style=for-the-badge&logo=Java)](https://openjdk.org/projects/jdk/17/)
[![GitHub Actions Status](https://img.shields.io/github/actions/workflow/status/onecx/onecx-quarkus/build.yml?logo=GitHub&style=for-the-badge)](https://github.com/onecx/onecx-quarkus/actions/workflows/build.yml)

## Getting started

Include the following bom artifact into your pom or parent pom and then pick the components you need.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.tkit.onecx.quarkus</groupId>
            <artifactId>onecx-quarkus-bom</artifactId>
            <version>${onecx.quarkus.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## Components

Include the component in your project by including the corresponding dependency.

:information_source: Some component come with additional documentation and configuration - check the 'Documentation' link for particular section.

| Name              | ArtifactId              | Description                          | Documentation                       |
|-------------------|-------------------------|--------------------------------------|-------------------------------------|
| Parameters        | onecx-parameters        | OneCX Parameter Service Extension    | [Doc](extensions/parameters)        |
| Tenant            | onecx-tenant            | OneCX Tenant Service Extension       | [Doc](extensions/tenant)            |
| Permissions       | onecx-permissions       | OneCX Permission Service Extension   | [Doc](extensions/permissions)       |
| OpenApi Generator | onecx-openapi-generator | OpenApi Generator Template Extension | [Doc](extensions/openapi-generator) |
| Security          | onecx-security          | OneCX Security Extension             | [Doc](extensions/security)          |

