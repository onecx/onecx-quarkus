package org.tkit.onecx.quarkus.it.operator;

import java.util.Objects;

import jakarta.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tkit.onecx.quarkus.operator.OperatorUtils;

import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.processing.event.source.filter.OnAddFilter;
import io.javaoperatorsdk.operator.processing.event.source.filter.OnUpdateFilter;

@ControllerConfiguration(name = "permission", namespaces = Constants.WATCH_CURRENT_NAMESPACE, onAddFilter = PermissionController.AddFilter.class, onUpdateFilter = PermissionController.UpdateFilter.class, generationAwareEventProcessing = false)
public class PermissionController implements Reconciler<Permission>, ErrorStatusHandler<Permission> {

    private static final Logger log = LoggerFactory.getLogger(PermissionController.class);

    @Override
    public ErrorStatusUpdateControl<Permission> updateErrorStatus(Permission permission, Context<Permission> context,
            Exception e) {
        int responseCode = -1;
        if (e.getCause() instanceof WebApplicationException re) {
            responseCode = re.getResponse().getStatus();
        }
        var status = new PermissionStatus();
        status.setAppId(null);
        status.setResponseCode(responseCode);
        status.setStatus(PermissionStatus.Status.ERROR);
        status.setMessage(e.getMessage());
        permission.setStatus(status);
        return ErrorStatusUpdateControl.updateStatus(permission);
    }

    @Override
    public UpdateControl<Permission> reconcile(Permission permission, Context<Permission> context) throws Exception {
        int responseCode = 200;
        if (Objects.equals(permission.getMetadata().getName(), "empty-spec")
                || Objects.equals(permission.getMetadata().getName(), "client-error")) {
            throw new WebApplicationException();
        }
        updateStatusPojo(permission, responseCode);
        return UpdateControl.updateStatus(permission);
    }

    private void updateStatusPojo(Permission permission, int responseCode) {
        PermissionStatus result = new PermissionStatus();
        PermissionSpec spec = permission.getSpec();
        result.setAppId(spec.getAppId());
        result.setResponseCode(responseCode);
        var status = responseCode == 200 ? PermissionStatus.Status.UPDATED : PermissionStatus.Status.UNDEFINED;
        result.setStatus(status);
        permission.setStatus(result);
    }

    public static class AddFilter implements OnAddFilter<Permission> {

        @Override
        public boolean accept(Permission resource) {
            return OperatorUtils.shouldProcessAdd(resource);
        }
    }

    public static class UpdateFilter implements OnUpdateFilter<Permission> {

        @Override
        public boolean accept(Permission newResource, Permission oldResource) {
            return OperatorUtils.shouldProcessUpdate(newResource, oldResource);
        }
    }

}
