package tv.notube.applications.model;

import tv.notube.applications.ApplicationsManager;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This class represents the permissions an applications (represented with its
 * UUID) has regarding some specific objects (such as profiles, users or activities).
 *
 * @author Davide Palmisano ( dpalmisano@gmail.com )
 */
public class Permissions implements Serializable {

    static final long serialVersionUID = 182012396185837510L;

    private Set<Permission> permissions = new HashSet<Permission>();

    private class Permission {

        private ApplicationsManager.Object object;

        private ApplicationsManager.Ownership ownership;

        private ApplicationsManager.Action action;

        private Permission(
                ApplicationsManager.Object object,
                ApplicationsManager.Ownership ownership,
                ApplicationsManager.Action action
        ) {
            this.object = object;
            this.ownership = ownership;
            this.action = action;
        }

        @Override
        public boolean equals(java.lang.Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Permission that = (Permission) o;

            if (action != that.action) return false;
            if (object != that.object) return false;
            if (ownership != that.ownership) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = object != null ? object.hashCode() : 0;
            result = 31 * result + (ownership != null ? ownership.hashCode() : 0);
            result = 31 * result + (action != null ? action.hashCode() : 0);
            return result;
        }
    }

    public void addPermission(
            ApplicationsManager.Action action,
            ApplicationsManager.Object object
    ) {
        permissions.add(
                new Permission(object, ApplicationsManager.Ownership.OWN, action)
        );
    }

    protected void addPermission(
            ApplicationsManager.Action action,
            ApplicationsManager.Ownership ownership,
            ApplicationsManager.Object object
    ) {
        permissions.add(
                new Permission(object, ownership, action)
        );
    }

    public boolean hasPermission(ApplicationsManager.Action action, Object object) {
        for (Permission permission : permissions) {
            if (permission.action.equals(action) && permission.object.equals(object)) {
                return true;
            }
        }
        return false;
    }

    public static Permissions buildDefault() {
        Permissions permissions = new Permissions();
        for (ApplicationsManager.Action action : ApplicationsManager.Action.values()) {
            for (ApplicationsManager.Object object : ApplicationsManager.Object.values()) {
                permissions.addPermission(action, object);
            }
        }
        return permissions;
    }

    public static Permissions buildRoot(UUID applicationId) {
        Permissions permissions = new Permissions();
        for (ApplicationsManager.Action action : ApplicationsManager.Action.values()) {
            for (ApplicationsManager.Object object : ApplicationsManager.Object.values()) {
                // add even the possibility to see other's stuff
                permissions.addPermission(action, ApplicationsManager.Ownership.OTHER, object);
                permissions.addPermission(action, object);
            }
        }
        return permissions;
    }

}
