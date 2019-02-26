/**
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.talend.sdk.component.server.front;

import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.talend.sdk.component.container.Container;
import org.talend.sdk.component.design.extension.RepositoryModel;
import org.talend.sdk.component.design.extension.repository.Config;
import org.talend.sdk.component.runtime.internationalization.FamilyBundle;
import org.talend.sdk.component.runtime.manager.ComponentManager;
import org.talend.sdk.component.server.api.ConfigurationTypeResource;
import org.talend.sdk.component.server.dao.ConfigurationDao;
import org.talend.sdk.component.server.front.model.ConfigTypeNode;
import org.talend.sdk.component.server.front.model.ConfigTypeNodes;
import org.talend.sdk.component.server.front.model.ErrorDictionary;
import org.talend.sdk.component.server.front.model.SimplePropertyDefinition;
import org.talend.sdk.component.server.front.model.error.ErrorPayload;
import org.talend.sdk.component.server.service.ActionsService;
import org.talend.sdk.component.server.service.ExtensionComponentMetadataManager;
import org.talend.sdk.component.server.service.LocaleMapper;
import org.talend.sdk.component.server.service.PropertiesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ConfigurationTypeResourceImpl implements ConfigurationTypeResource {

    @Inject
    private ComponentManager manager;

    @Inject
    private PropertiesService propertiesService;

    @Inject
    private ActionsService actionsService;

    @Inject
    private LocaleMapper localeMapper;

    @Inject
    private ConfigurationDao configurations;

    @Inject
    private ExtensionComponentMetadataManager virtualComponents;

    @Override
    public ConfigTypeNodes getRepositoryModel(final String language, final boolean lightPaylaod) {
        return toNodes(language, s -> true, lightPaylaod);
    }

    @Override
    public ConfigTypeNodes getDetail(final String language, final String[] ids) {
        final Predicate<String> filter = ids == null ? s -> false : new Predicate<String>() {

            private final Collection<String> values = Stream.of(ids).collect(toSet());

            @Override
            public boolean test(final String s) {
                return values.contains(s);
            }
        };

        return toNodes(language, filter, false);
    }

    @Override
    public Map<String, String> migrate(final String id, final int version, final Map<String, String> config) {
        if (virtualComponents.isExtensionEntity(id)) {
            return config;
        }
        final Config configuration = ofNullable(configurations.findById(id))
                .orElseThrow(() -> new WebApplicationException(Response
                        .status(Response.Status.NOT_FOUND)
                        .entity(new ErrorPayload(ErrorDictionary.CONFIGURATION_MISSING,
                                "Didn't find configuration " + id))
                        .build()));
        final Map<String, String> configToMigrate = new HashMap<>(config);
        final String versionKey = configuration.getMeta().getPath() + ".__version";
        final boolean addedVersion = configToMigrate.putIfAbsent(versionKey, Integer.toString(version)) == null;
        final Map<String, String> migrated = configuration.getMigrationHandler().migrate(version, configToMigrate);
        if (addedVersion) {
            migrated.remove(versionKey);
        }
        return migrated;
    }

    private Stream<ConfigTypeNode> createNode(final String parentId, final String family, final Stream<Config> configs,
            final FamilyBundle resourcesBundle, final Container container, final Locale locale,
            final Predicate<String> idFilter, final boolean lightPayload) {
        final ClassLoader loader = container.getLoader();
        if (configs == null) {
            return Stream.empty();
        }
        return configs.flatMap(c -> {
            final Stream<ConfigTypeNode> configNode;
            if (idFilter.test(c.getId())) {
                final ConfigTypeNode node = new ConfigTypeNode();
                node.setId(c.getId());
                node.setVersion(c.getVersion());
                node.setConfigurationType(c.getKey().getConfigType());
                node.setName(c.getKey().getConfigName());
                node.setParentId(parentId);
                node
                        .setDisplayName(resourcesBundle
                                .configurationDisplayName(c.getKey().getConfigType(), c.getKey().getConfigName())
                                .orElse(c.getKey().getConfigName()));
                if (!lightPayload) {
                    node.setActions(actionsService.findActions(family, container, locale, c, resourcesBundle));

                    // force configuration as root prefix
                    final int prefixLen = c.getMeta().getPath().length();
                    final String forcedPrefix = c.getMeta().getName();
                    node
                            .setProperties(propertiesService
                                    .buildProperties(singletonList(c.getMeta()), loader, locale, null)
                                    .map(p -> new SimplePropertyDefinition(
                                            forcedPrefix + p.getPath().substring(prefixLen), p.getName(),
                                            p.getDisplayName(), p.getType(), p.getDefaultValue(), p.getValidation(),
                                            p.getMetadata(), p.getPlaceholder(), p.getProposalDisplayNames()))
                                    .collect(toList()));
                }

                node.setEdges(c.getChildConfigs().stream().map(Config::getId).collect(toSet()));

                configNode = Stream.of(node);
            } else {
                configNode = Stream.empty();
            }

            return Stream
                    .concat(configNode, createNode(c.getId(), family, c.getChildConfigs().stream(), resourcesBundle,
                            container, locale, idFilter, lightPayload));
        });
    }

    private ConfigTypeNodes toNodes(final String language, final Predicate<String> filter, final boolean lightPayload) {
        final Locale locale = localeMapper.mapLocale(language);
        return new ConfigTypeNodes(Stream
                .concat(getDeployedConfigurations(filter, lightPayload, locale),
                        virtualComponents
                                .getConfigurations()
                                .stream()
                                .filter(it -> filter.test(it.getId()))
                                .map(it -> lightPayload ? copyLight(it) : it))
                .collect(toMap(ConfigTypeNode::getId, identity())));
    }

    private ConfigTypeNode copyLight(final ConfigTypeNode it) {
        return new ConfigTypeNode(it.getId(), it.getVersion(), it.getParentId(), it.getConfigurationType(),
                it.getName(), it.getDisplayName(), it.getEdges(), null, null);
    }

    private Stream<ConfigTypeNode> getDeployedConfigurations(final Predicate<String> filter, final boolean lightPayload,
            final Locale locale) {
        return manager
                .find(Stream::of)
                .filter(c -> c.get(RepositoryModel.class) != null)
                .flatMap(c -> c
                        .get(RepositoryModel.class)
                        .getFamilies()
                        .stream()
                        .filter(f -> !f.getConfigs().isEmpty())
                        .flatMap(family -> {
                            final FamilyBundle resourcesBundle = family.getMeta().findBundle(c.getLoader(), locale);

                            final Stream<ConfigTypeNode> familyNode;
                            if (filter.test(family.getId())) {
                                final ConfigTypeNode node = new ConfigTypeNode();
                                node.setId(family.getId());
                                node.setName(family.getMeta().getName());

                                node.setDisplayName(resourcesBundle.displayName().orElse(family.getMeta().getName()));

                                node.setEdges(family.getConfigs().stream().map(Config::getId).collect(toSet()));
                                familyNode = Stream.of(node);
                            } else {
                                familyNode = Stream.empty();
                            }
                            return Stream
                                    .concat(familyNode,
                                            createNode(family.getId(), family.getMeta().getName(),
                                                    family.getConfigs().stream(), resourcesBundle, c, locale, filter,
                                                    lightPayload));
                        }));
    }
}
