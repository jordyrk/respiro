/*
 * Copyright 2015 Kantega AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kantega.respiro;

import org.kantega.respiro.api.ApplicationBuilder;
import org.kantega.respiro.api.DataSourceBuilder;
import org.kantega.respiro.api.mail.MailConfigBuilder;
import org.kantega.respiro.api.mail.MailSender;
import org.kantega.reststop.api.Config;
import org.kantega.reststop.api.Export;
import org.kantega.reststop.api.Plugin;

import javax.annotation.PreDestroy;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.TopicConnectionFactory;
import javax.sql.DataSource;
import javax.ws.rs.core.Application;

/**
 *
 */
@Plugin
public class UserProfilePlugin {



    @Export final Application exampleApplication;

    private final TopicNotifier notifier;


    public UserProfilePlugin(@Config String jdbcDriverClass,
                             @Config String helloDatabaseUsername,
                             @Config String helloDatabasePassword,
                             @Config String helloDatabaseUrl,
                             @Config String smtpAddress,
                             @Config String smtpFrom,
                             @Config String smtpTo,
                             @Config(defaultValue = "25") int smtpPort,
                             ApplicationBuilder builder,
                             DataSourceBuilder dsBuilder,
                             MailConfigBuilder mailConfigBuilder,
                             TopicConnectionFactory connectionFactory) throws JMSException {

        DataSource myDataSource = dsBuilder.datasource(helloDatabaseUrl)
                .username(helloDatabaseUsername).password(helloDatabasePassword).driverClassname(jdbcDriverClass).build();
        UsersDAO dao = new UsersDAO(myDataSource);

        MailSender sender = mailConfigBuilder.server(smtpAddress,smtpPort).from(smtpFrom).to(smtpTo).build();
        notifier = new TopicNotifier(connectionFactory);
        exampleApplication = builder.application().singleton(new UserProfileResource(dao, sender, notifier)).build();

    }


    @PreDestroy
    public void destroy() {
        notifier.close();
    }
}