package db.migration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.onap.so.db.catalog.beans.CloudIdentity;
import org.onap.so.db.catalog.beans.CloudSite;
import org.onap.so.db.catalog.beans.CloudifyManager;
import org.onap.so.logger.MsoLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;

/**
 * Performs migration using JDBC Connection from the cloud config provided in the environment (application-{profile}.yaml) and persist data (when not already present) to the catalod database.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class R__CloudConfigMigration implements JdbcMigration , MigrationInfoProvider, MigrationChecksumProvider {
    public static final String FLYWAY = "FLYWAY";

    private static final MsoLogger LOGGER = MsoLogger.getMsoLogger(MsoLogger.Catalog.RA, R__CloudConfigMigration.class);
    @JsonProperty("cloud_config")
    private CloudConfig cloudConfig;

    @Override
    public void migrate(Connection connection) throws Exception {
        LOGGER.debug("Starting migration for CloudConfig");
        CloudConfig cloudConfig = loadCloudConfig();
        if(cloudConfig == null){
            LOGGER.debug("No CloudConfig defined in :"+getApplicationYamlName()+" exiting.");
        }else{
            migrateCloudIdentity(cloudConfig.getIdentityServices().values(), connection);
            migrateCloudSite(cloudConfig.getCloudSites().values(), connection);
            migrateCloudifyManagers(cloudConfig.getCloudifyManagers().values(), connection);
        }
    }

    public CloudConfig getCloudConfig() {
        return cloudConfig;
    }

    public void setCloudConfig(CloudConfig cloudConfig) {
        this.cloudConfig = cloudConfig;
    }

    private CloudConfig loadCloudConfig() throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        R__CloudConfigMigration cloudConfigMigration = mapper.readValue(R__CloudConfigMigration.class
                .getResourceAsStream(getApplicationYamlName()), R__CloudConfigMigration.class);
        CloudConfig cloudConfig = cloudConfigMigration.getCloudConfig();
        if(cloudConfig != null){
            cloudConfig.populateId();
        }

        return cloudConfig;
    }

    private String getApplicationYamlName() {
        String profile = System.getProperty("spring.profiles.active") == null ? "" : "-" + System.getProperty("spring.profiles.active");
        return "/application" + profile + ".yaml";
    }

    private void migrateCloudIdentity(Collection<CloudIdentity> entities, Connection connection) throws Exception {
        LOGGER.debug("Starting migration for CloudConfig-->IdentityService");
        String insert = "INSERT INTO `identity_services` (`ID`, `IDENTITY_URL`, `MSO_ID`, `MSO_PASS`, `ADMIN_TENANT`, `MEMBER_ROLE`, `TENANT_METADATA`, `IDENTITY_SERVER_TYPE`, `IDENTITY_AUTHENTICATION_TYPE`, `LAST_UPDATED_BY`) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?);";
        PreparedStatement ps = connection.prepareStatement(insert);
        try (Statement stmt = connection.createStatement()) {
            for (CloudIdentity cloudIdentity : entities) {
                try (ResultSet rows = stmt.executeQuery("Select count(1) from identity_services where id='" + cloudIdentity.getId() + "'")) {
                    int count = 0;
                    while (rows.next()) {
                        count = rows.getInt(1);
                    }
                    if (count == 0) {
                        ps.setString(1, cloudIdentity.getId());
                        ps.setString(2, cloudIdentity.getIdentityUrl());
                        ps.setString(3, cloudIdentity.getMsoId());
                        ps.setString(4, cloudIdentity.getMsoPass());
                        ps.setString(5, cloudIdentity.getAdminTenant());
                        ps.setString(6, cloudIdentity.getMemberRole());
                        ps.setBoolean(7, cloudIdentity.getTenantMetadata());
                        ps.setString(8, cloudIdentity.getIdentityServerType() != null ? cloudIdentity.getIdentityServerType().name() : null);
                        ps.setString(9, cloudIdentity.getIdentityAuthenticationType() != null ? cloudIdentity.getIdentityAuthenticationType().name() : null);
                        ps.setString(10, FLYWAY);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    private void migrateCloudSite(Collection<CloudSite> entities, Connection connection) throws Exception {
        LOGGER.debug("Starting migration for CloudConfig-->CloudSite");
        String insert = "INSERT INTO `cloud_sites` (`ID`, `REGION_ID`, `IDENTITY_SERVICE_ID`, `CLOUD_VERSION`, `CLLI`, `CLOUDIFY_ID`, `PLATFORM`, `ORCHESTRATOR`, `LAST_UPDATED_BY`) " +
                "VALUES (?,?,?,?,?,?,?,?,?);";
        PreparedStatement ps = connection.prepareStatement(insert);
        try (Statement stmt = connection.createStatement()) {
            for (CloudSite cloudSite : entities) {
                try (ResultSet rows = stmt.executeQuery("Select count(1) from cloud_sites where id='" + cloudSite.getId() + "'")) {
                    int count = 0;
                    while (rows.next()) {
                        count = rows.getInt(1);
                    }
                    if (count == 0) {
                        ps.setString(1, cloudSite.getId());
                        ps.setString(2, cloudSite.getRegionId());
                        ps.setString(3, cloudSite.getIdentityServiceId());
                        ps.setString(4, cloudSite.getCloudVersion());
                        ps.setString(5, cloudSite.getClli());
                        ps.setString(6, cloudSite.getCloudifyId());
                        ps.setString(7, cloudSite.getPlatform());
                        ps.setString(8, cloudSite.getOrchestrator());
                        ps.setString(9, FLYWAY);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    private void migrateCloudifyManagers(Collection<CloudifyManager> entities, Connection connection) throws Exception {
        String insert = "INSERT INTO `cloudify_managers` (`ID`, `CLOUDIFY_URL`, `USERNAME`, `PASSWORD`, `VERSION`, `LAST_UPDATED_BY`)" +
                " VALUES (?,?,?,?,?,?);";
        PreparedStatement ps = connection.prepareStatement(insert);
        try (Statement stmt = connection.createStatement()) {
            for (CloudifyManager cloudifyManager : entities) {
                try (ResultSet rows = stmt.executeQuery("Select count(1) from cloudify_managers where id='" + cloudifyManager.getId() + "'")) {
                    int count = 0;
                    while (rows.next()) {
                        count = rows.getInt(1);
                    }
                    if (count == 0) {
                        ps.setString(1, cloudifyManager.getId());
                        ps.setString(2, cloudifyManager.getCloudifyUrl());
                        ps.setString(3, cloudifyManager.getUsername());
                        ps.setString(4, cloudifyManager.getPassword());
                        ps.setString(5, cloudifyManager.getVersion());
                        ps.setString(6, FLYWAY);
                        ps.executeUpdate();
                    }
                }
            }
        }
    }

    public MigrationVersion getVersion() {
        return null;
    }

    public String getDescription() {
        return "R_CloudConfigMigration";
    }

    public Integer getChecksum() {
        return Math.toIntExact(System.currentTimeMillis() / 1000);
    }
}

