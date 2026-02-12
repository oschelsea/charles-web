package io.charles.project.monitor.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.charles.common.utils.poi.ExcelUtil;
import io.charles.framework.aspectj.lang.annotation.Log;
import io.charles.framework.aspectj.lang.enums.BusinessType;
import io.charles.framework.web.controller.BaseController;
import io.charles.framework.web.domain.R;
import io.charles.framework.web.page.TableDataInfo;
import io.charles.project.monitor.domain.SysLogininfor;
import io.charles.project.monitor.service.ISysLogininforService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * 系统访问记录
 *
 * @author charles
 */
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RestController
@RequestMapping("/monitor/logininfor")
public class SysLogininforController extends BaseController {
    private final ISysLogininforService logininforService;

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysLogininfor logininfor) {
        Page<SysLogininfor> page = getPage();
        logininforService.selectLogininforList(page, logininfor);
        return getDataTable(page);
    }

    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('monitor:logininfor:export')")
    @PostMapping("/export")
    public void export(SysLogininfor logininfor, HttpServletResponse response) throws IOException {
        List<SysLogininfor> list = logininforService.selectLogininforList(null, logininfor);
        ExcelUtil<SysLogininfor> util = new ExcelUtil<SysLogininfor>(SysLogininfor.class);
        util.exportExcel(response, list, "登录日志");
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public R<Void> remove(@PathVariable Long[] infoIds) {
        return toResult(logininforService.deleteLogininforByIds(infoIds));
    }

    @PreAuthorize("@ss.hasPermi('monitor:logininfor:remove')")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        logininforService.cleanLogininfor();
        return R.ok();
    }
}
