package toilet.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import libWebsiteTools.JVMNotSupportedError;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.security.HashUtil;
import toilet.bean.ToiletBeanAccess;

/**
 * Servlets that require a login to use must extend this class. Also provides
 * functionality that other servlets can use to verify logins for features that
 * require authorization.
 *
 * @author alpha
 * @see toilet.servlet.AdminServletPermission
 */
public abstract class AdminServlet extends ToiletServlet {

    /**
     * I couldn't get annotations to work with enums, so subclasses must
     * implement this.
     *
     * @param req
     * @return The requisite permission for the given request.
     */
    public abstract AdminServletPermission getRequiredPermission(HttpServletRequest req);

    /**
     *
     * @param req
     * @return Is the user authorized to make this request?
     */
    public boolean isAuthorized(HttpServletRequest req) {
        return isAuthorized(req, getRequiredPermission(req));
    }

    /**
     * @param req
     * @param permission
     * @return Does the session have the given scope saved, or does the
     * request's Authorization header have the password for the given
     * permission?
     */
    public static boolean isAuthorized(HttpServletRequest req, AdminServletPermission permission) {
        if (permission.equals(req.getSession().getAttribute(AdminServletPermission.class.getCanonicalName()))) {
            return true;
        }
        try {
            String authHeader = req.getHeader(HttpHeaders.AUTHORIZATION).substring(6);
            String decoded = new String(Base64.getDecoder().decode(authHeader), "UTF-8");
            String[] parts = decoded.split(":", 2);
            if (1 < parts.length) {
                return null != new PasswordChecker(ToiletBeanAccess.getStaticInstance(req).getImead(), permission, parts[1]).call();
            }
        } catch (UnsupportedEncodingException ex) {
            throw new JVMNotSupportedError(ex);
        } catch (NullPointerException n) {
        }
        return false;
    }

    /**
     * Check if the given password matches any password for any scope, and save
     * scope to session. If none found, clear any existing scope from session.
     * Used by login page. Will use multiple threads to test all scopes in
     * parallel.
     *
     * @param req
     * @param password
     * @return scope that password matches, or null if none found.
     */
    public AdminServletPermission authorize(HttpServletRequest req, String password) {
        ToiletBeanAccess beans = allBeans.getInstance(req);
        List<Future<AdminServletPermission>> checkers = new ArrayList<>(AdminServletPermission.values().length);
        HttpSession sess = req.getSession();
        try {
            for (AdminServletPermission per : AdminServletPermission.values()) {
                checkers.add(beans.getExec().submit(new PasswordChecker(beans.getImead(), per, password)));
            }
            for (Future<AdminServletPermission> test : checkers) {
                if (null != test.get()) {
                    sess.setAttribute(AdminServletPermission.class.getCanonicalName(), test.get());
                    return test.get();
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            sess.removeAttribute(AdminServletPermission.class.getCanonicalName());
            throw new RuntimeException("Something went wrong while verifying passwords.", ex);
        }
        sess.removeAttribute(AdminServletPermission.class.getCanonicalName());
        return null;
    }

    @Override
    protected void service​(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setHeader(HttpHeaders.CACHE_CONTROL, "private, no-store");
        res.setDateHeader(HttpHeaders.EXPIRES, OffsetDateTime.now().toInstant().toEpochMilli());
        if (isAuthorized(req)) {
            super.service(req, res);
        } else {
            res.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + getRequiredPermission(req).getKey() + "\", charset=\"UTF-8\"");
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}

class PasswordChecker implements Callable<AdminServletPermission> {

    private final String hash;
    private final String toVerify;
    private final AdminServletPermission scope;

    public PasswordChecker(IMEADHolder imead, AdminServletPermission scope, String toVerify) {
        this.hash = imead.getValue(scope.getKey());
        this.toVerify = toVerify;
        this.scope = scope;
        if (null == this.hash) {
            throw new IllegalArgumentException("No hash available at " + scope.getKey());
        }
        if (null == toVerify) {
            throw new IllegalArgumentException("No password to check.");
        }
        if (null == scope) {
            throw new IllegalArgumentException("No permission passed to check.");
        }
    }

    @Override
    public AdminServletPermission call() {
        return HashUtil.verifyArgon2Hash(hash, toVerify) ? scope : null;
    }
}
