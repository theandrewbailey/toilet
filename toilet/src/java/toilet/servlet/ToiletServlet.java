package toilet.servlet;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import libOdyssey.bean.ExceptionRepo;
import libWebsiteTools.file.FileRepo;
import libWebsiteTools.imead.IMEADHolder;
import libWebsiteTools.tag.HtmlCss;
import libWebsiteTools.tag.HtmlScript;
import toilet.bean.EntryRepo;
import toilet.bean.StateCache;
import toilet.bean.UtilBean;
import toilet.db.Article;

/**
 *
 * @author alpha
 */
public abstract class ToiletServlet extends HttpServlet {

    @Resource
    protected ManagedExecutorService exec;
    @EJB
    protected ExceptionRepo error;
    @EJB
    protected FileRepo file;
    @EJB
    protected EntryRepo entry;
    @EJB
    protected StateCache cache;
    @EJB
    protected IMEADHolder imead;
    @EJB
    protected UtilBean util;

    protected void asyncFiles(HttpServletRequest req) {
        req.setAttribute("asyncFiles", exec.submit(() -> {
            HtmlCss.getCssFiles(req, imead, file);
            HtmlScript.getJavascriptFiles(req, imead, file);
            return true;
        }));
    }

    protected void asyncRecentCategories(HttpServletRequest req, String... categories) {
        req.setAttribute("asyncCats", exec.submit(() -> {
            Map<String, List<Article>> cats = new LinkedHashMap<>(5);
            for (String section : categories) {
                if (null != section && 0 != section.length()) {
                    cats.put(section, entry.getSection(section, 1, 10));
                }
            }
            cats.put("", entry.getSection(null, 1, 10));
            return cats;
        }));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doTrace(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
