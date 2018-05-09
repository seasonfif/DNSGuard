package com.sogou.dnsguard;

import java.util.HashSet;
import java.util.Set;

public class GuardConfig {

    @LogLevel
    public int level;

    public GuardianMap guardianMap;

    public static class Builder{

        @LogLevel
        private int level;
        private Set<Guardian> guardians;

        public Builder(){
            guardians = new HashSet<>();
        }

        public Builder setLogLevel(@LogLevel int level){
            this.level = level;
            return this;
        }

        public Builder addGuardian(Guardian guardian){
            if (guardian != null){
                guardians.add(guardian);
            }
            return this;
        }

        public GuardConfig build(){
            GuardConfig config = new GuardConfig();

            config.level = level;
            config.guardianMap = generateGuardianMap(guardians);

            return config;
        }

        private GuardianMap generateGuardianMap(Set<Guardian> guardians) {
            GuardianMap guardianMap = new GuardianMap();

            for (Guardian guardian : guardians) {
                if (guardian.domainName != null){
                    guardianMap.put(guardian.domainName, guardian);
                }
            }

            return guardianMap;
        }
    }
}
