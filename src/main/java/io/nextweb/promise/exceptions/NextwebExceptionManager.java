package io.nextweb.promise.exceptions;

import io.nextweb.promise.Fn;

public class NextwebExceptionManager implements ExceptionInterceptor<NextwebExceptionManager>,
UnauthorizedInterceptor<NextwebExceptionManager>, ExceptionListener, UnauthorizedListener, UndefinedListener,
ImpossibleListener, ImpossibleInterceptor<NextwebExceptionManager>,
        UndefinedInterceptor<NextwebExceptionManager> {

    public static NextwebExceptionManager fallbackExceptionManager;

    private UnauthorizedListener authExceptionListener;
    private ExceptionListener exceptionListener;
    private UndefinedListener undefinedExceptionListener;
    private ImpossibleListener impossibleListener;

    private final NextwebExceptionManager parentExceptionManager;

    @Override
    public NextwebExceptionManager catchUnauthorized(final UnauthorizedListener authExceptionListener) {
        this.authExceptionListener = authExceptionListener;
        return this;
    }

    @Override
    public NextwebExceptionManager catchExceptions(final ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
        return this;
    }

    public boolean canCatchExceptions() {
        return this.exceptionListener != null
                || (this.parentExceptionManager != null && this.parentExceptionManager.canCatchExceptions());

    }

    public boolean canCatchUndefinedExceptions() {
        return this.undefinedExceptionListener != null || canCatchExceptions()
                || (this.parentExceptionManager != null && this.parentExceptionManager.canCatchUndefinedExceptions());

    }

    public boolean canCatchAuthorizationExceptions() {
        return this.authExceptionListener != null
                || canCatchExceptions()
                || (this.parentExceptionManager != null && this.parentExceptionManager
                .canCatchAuthorizationExceptions());

    }

    public boolean canCatchImpossibe() {
        return this.impossibleListener != null || canCatchExceptions()
                || (this.parentExceptionManager != null && this.parentExceptionManager.canCatchImpossibe());
    }

    @Override
    public void onFailure(final ExceptionResult r) {
        final Throwable o = r.exception();

        if (o instanceof ImpossibleException) {

            final ImpossibleException ie = (ImpossibleException) o;
            onImpossible(ir);
            return;
        }

        if (o instanceof UndefinedException) {
            final UndefinedException ue = (UndefinedException) o;
            exceptionManager.onUndefined(ue.getResult());
            return;
        }

        if (o instanceof UnauthorizedException) {
            final UnauthorizedException ue = (UnauthorizedException) o;
            exceptionManager.onUnauthorized(ue.getResult());
            return;

        }

        if (this.exceptionListener != null) {
            this.exceptionListener.onFailure(r);
            return;
        }

        if (this.parentExceptionManager != null) {
            if (this.parentExceptionManager.canCatchExceptions()) {
                this.parentExceptionManager.onFailure(r);
                return;
            }
        }

        fallbackExceptionManager.onFailure(r);

    }

    @Override
    public void onUnauthorized(final UnauthorizedResult r) {
        // assert canCatchAuthorizationExceptions() || canCatchExceptions();

        if (this.authExceptionListener != null) {
            this.authExceptionListener.onUnauthorized(r);
            return;
        }

        if (this.exceptionListener != null) {
            this.exceptionListener
            .onFailure(Fn.exception(r.origin(), new Exception("Unauthorized: " + r.getMessage())));
            return;
        }

        if (this.parentExceptionManager != null) {
            if (this.parentExceptionManager.canCatchAuthorizationExceptions()) {
                this.parentExceptionManager.onUnauthorized(r);
                return;
            }
        }

        onFailure(Fn.exception(r.origin(), new Exception("Unauthorized: " + r.getMessage())));
    }

    @Override
    public void onImpossible(final ImpossibleResult ir) {
        // assert canCatchImpossibe() || canCatchExceptions();

        if (this.impossibleListener != null) {
            this.impossibleListener.onImpossible(ir);
            return;
        }

        if (this.exceptionListener != null) {
            this.exceptionListener.onFailure(Fn.exception(ir.origin(),
                    new Exception("Operation impossible: [" + ir.message() + "]")));
            return;
        }

        if (this.parentExceptionManager != null) {
            if (this.parentExceptionManager.canCatchImpossibe()) {
                this.parentExceptionManager.onImpossible(ir);
                return;
            }
        }

        onFailure(Fn.exception(ir.origin(), new Exception("Operation impossible: [" + ir.message() + "]")));
    }

    @Override
    public NextwebExceptionManager catchImpossible(final ImpossibleListener listener) {

        this.impossibleListener = listener;
        return this;
    }

    @Override
    public NextwebExceptionManager catchUndefined(final UndefinedListener undefinedExceptionListener) {
        this.undefinedExceptionListener = undefinedExceptionListener;
        return this;
    }

    @Override
    public void onUndefined(final UndefinedResult r) {
        // assert canCatchUndefinedExceptions() || canCatchExceptions();

        if (this.undefinedExceptionListener != null) {
            this.undefinedExceptionListener.onUndefined(r);
            return;
        }

        if (this.exceptionListener != null) {
            this.exceptionListener.onFailure(Fn.exception(r.origin(), new Exception("Undefined: " + r.message())));
            return;
        }

        if (this.parentExceptionManager != null) {
            if (this.parentExceptionManager.canCatchUndefinedExceptions()) {
                this.parentExceptionManager.onUndefined(r);
                return;
            }
        }

        onFailure(Fn.exception(r.origin(), new Exception("Undefined: " + r.message())));
    }

    public NextwebExceptionManager(final NextwebExceptionManager parentExceptionManager) {
        super();
        this.parentExceptionManager = parentExceptionManager;

    }

}