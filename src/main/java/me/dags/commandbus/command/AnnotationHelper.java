package me.dags.commandbus.command;

import me.dags.commandbus.annotation.Assignment;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author dags <dags@dags.me>
 */
class AnnotationHelper {

    static Description getDescription(Method method) {
        Description description = method.getAnnotation(Description.class);
        return description != null ? description : DESCRIPTION;
    }

    static Permission getPermission(String id, Method method) {
        Command command = method.getAnnotation(Command.class);
        Assignment assignment = getAssignment(method);
        Permission permission = getPermission(method);

        String node = permission.value();

        // @Permission annotation present but no string specified == auto-generate one
        if (node.isEmpty() && permission != PERMISSION) {
            if (command.parent().isEmpty()) {
                node = String.format("%s.command.%s", id, command.alias()[0]).replace(' ', '.');
            } else {
                node = String.format("%s.command.%s.%s", id, command.parent(), command.alias()[0]).replace(' ', '.');
            }
        }

        final String permNode = node;
        final String permDesc = permission.description();
        final Assignment permAssign = assignment != ASSIGNMENT ? assignment : permission.assign();

        return new Permission(){
            @Override
            public Class<? extends Annotation> annotationType() {
                return Permission.class;
            }

            @Override
            public String value() {
                return permNode;
            }

            @Override
            public String description() {
                return permDesc;
            }

            @Override
            public Assignment assign() {
                return permAssign;
            }
        };
    }

    private static Assignment getAssignment(Method method) {
        Assignment assignment = method.getAnnotation(Assignment.class);
        return assignment != null ? assignment : ASSIGNMENT;
    }

    private static Permission getPermission(Method method) {
        Permission permission = method.getAnnotation(Permission.class);
        return permission != null ? permission : PERMISSION;
    }

    private static final Assignment ASSIGNMENT = new Assignment() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Assignment.class;
        }

        @Override
        public String role() {
            return "";
        }

        @Override
        public boolean permit() {
            return false;
        }
    };

    private static final Description DESCRIPTION = new Description() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Description.class;
        }

        @Override
        public String value() {
            return "";
        }
    };

    private static final Permission PERMISSION = new Permission() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Permission.class;
        }

        @Override
        public String value() {
            return "";
        }

        @Override
        public String description() {
            return "";
        }

        @Override
        public Assignment assign() {
            return ASSIGNMENT;
        }
    };
}
