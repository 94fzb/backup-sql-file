<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <title>${_plugin.name} - V${_plugin.version} - 记录查看</title>
    <link rel="stylesheet" href="assets/css/bootstrap.min.css"/>
    <style>
        body {
            margin: 0;
            font-size: 16px;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, 'Noto Sans', sans-serif, 'Apple Color Emoji', 'Segoe UI Emoji', 'Segoe UI Symbol', 'Noto Color Emoji';
            font-variant: tabular-nums;
            line-height: 1.5;
            font-feature-settings: 'tnum';
            background: transparent;
        }
    </style>
</head>
<body>
<div class="main-container">
    <div class="col-xs-8" style="padding: 0">
    <h3>查看文件列表 (仅保留最近${maxKeepSize}条记录)</h3>
        <hr/>
        <table class="table table-bordered table-striped">
            <thead>
            <tr>
                <th scope="col">#</th>
                <th scope="col">文件名</th>
                <th scope="col">创建时间</th>
                <th scope="col">文件大小</th>
            </tr>
            </thead>
            <tbody>
            <#list files as file>
            <tr>
                <th scope="row">${file.index}</th>
                <td><a href="downfile?file=${file.fileName}">${file.fileName}</a></td>
                <td>${file.lastModified}</td>
                <td>${file.size}</td>
            </tr>
            </#list>
            </tbody>
        </table>
        <hr/>
    </div>
</div>
</body>
</html>