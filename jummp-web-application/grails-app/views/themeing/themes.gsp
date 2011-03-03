<%@ page contentType="text/html;charset=UTF-8" %>
<form id="change-theme-form" action="saveTheme" method="POST">
    <g:select id="change-theme-themes" name="theme" from="${themes}" value="${selected}" noSelection="['' : g.message(code: 'theme.change.select')]"/>
    <input type="button" value="${g.message(code: 'theme.change.button')}" onclick="changeTheme()">
</form>
