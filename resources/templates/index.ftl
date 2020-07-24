<#-- @ftlvariable name="entries" type="kotlin.collections.List<com.jetbrains.handson.website.BlogEntry>" -->
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Approving Questions</title>
</head>
<body style="text-align: center; font-family: sans-serif">
<h1>Questions from MPS Slack Community </h1>
<hr>
<#list questionEntries as item>
    <div>
        <h3>${item.answers[3].text}</h3>
    </div>
</#list>
<hr>
<div>
    <h3>Add a new journal entry!</h3>
    <form action="/submit" method="post">
        <input type="text" name="headline">
        <br>
        <textarea name="body"></textarea>
        <br>
        <input type="submit">
    </form>
</div>
</body>
</html>