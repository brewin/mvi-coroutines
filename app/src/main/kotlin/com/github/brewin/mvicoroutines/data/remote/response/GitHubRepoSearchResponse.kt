package com.github.brewin.mvicoroutines.data.remote.response

import kotlinx.serialization.Optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubRepoSearchResponse(
    @Optional
    @SerialName("incomplete_results")
    val incompleteResults: Boolean? = null,
    @Optional
    @SerialName("items")
    val items: List<Item?>? = null,
    @Optional
    @SerialName("total_count")
    val totalCount: Int? = null
) {
    @Serializable
    data class Item(
        @Optional
        @SerialName("archive_url")
        val archiveUrl: String? = null,
        @Optional
        @SerialName("archived")
        val archived: Boolean? = null,
        @Optional
        @SerialName("assignees_url")
        val assigneesUrl: String? = null,
        @Optional
        @SerialName("blobs_url")
        val blobsUrl: String? = null,
        @Optional
        @SerialName("branches_url")
        val branchesUrl: String? = null,
        @Optional
        @SerialName("clone_url")
        val cloneUrl: String? = null,
        @Optional
        @SerialName("collaborators_url")
        val collaboratorsUrl: String? = null,
        @Optional
        @SerialName("comments_url")
        val commentsUrl: String? = null,
        @Optional
        @SerialName("commits_url")
        val commitsUrl: String? = null,
        @Optional
        @SerialName("compare_url")
        val compareUrl: String? = null,
        @Optional
        @SerialName("contents_url")
        val contentsUrl: String? = null,
        @Optional
        @SerialName("contributors_url")
        val contributorsUrl: String? = null,
        @Optional
        @SerialName("created_at")
        val createdAt: String? = null,
        @Optional
        @SerialName("default_branch")
        val defaultBranch: String? = null,
        @Optional
        @SerialName("deployments_url")
        val deploymentsUrl: String? = null,
        @Optional
        @SerialName("description")
        val description: String? = null,
        @Optional
        @SerialName("disabled")
        val disabled: Boolean? = null,
        @Optional
        @SerialName("downloads_url")
        val downloadsUrl: String? = null,
        @Optional
        @SerialName("events_url")
        val eventsUrl: String? = null,
        @Optional
        @SerialName("fork")
        val fork: Boolean? = null,
        @Optional
        @SerialName("forks")
        val forks: Int? = null,
        @Optional
        @SerialName("forks_count")
        val forksCount: Int? = null,
        @Optional
        @SerialName("forks_url")
        val forksUrl: String? = null,
        @Optional
        @SerialName("full_name")
        val fullName: String? = null,
        @Optional
        @SerialName("git_commits_url")
        val gitCommitsUrl: String? = null,
        @Optional
        @SerialName("git_refs_url")
        val gitRefsUrl: String? = null,
        @Optional
        @SerialName("git_tags_url")
        val gitTagsUrl: String? = null,
        @Optional
        @SerialName("git_url")
        val gitUrl: String? = null,
        @Optional
        @SerialName("has_downloads")
        val hasDownloads: Boolean? = null,
        @Optional
        @SerialName("has_issues")
        val hasIssues: Boolean? = null,
        @Optional
        @SerialName("has_pages")
        val hasPages: Boolean? = null,
        @Optional
        @SerialName("has_projects")
        val hasProjects: Boolean? = null,
        @Optional
        @SerialName("has_wiki")
        val hasWiki: Boolean? = null,
        @Optional
        @SerialName("homepage")
        val homepage: String? = null,
        @Optional
        @SerialName("hooks_url")
        val hooksUrl: String? = null,
        @Optional
        @SerialName("html_url")
        val htmlUrl: String? = null,
        @Optional
        @SerialName("id")
        val id: Int? = null,
        @Optional
        @SerialName("issue_comment_url")
        val issueCommentUrl: String? = null,
        @Optional
        @SerialName("issue_events_url")
        val issueEventsUrl: String? = null,
        @Optional
        @SerialName("issues_url")
        val issuesUrl: String? = null,
        @Optional
        @SerialName("keys_url")
        val keysUrl: String? = null,
        @Optional
        @SerialName("labels_url")
        val labelsUrl: String? = null,
        @Optional
        @SerialName("language")
        val language: String? = null,
        @Optional
        @SerialName("languages_url")
        val languagesUrl: String? = null,
        @Optional
        @SerialName("license")
        val license: License? = null,
        @Optional
        @SerialName("merges_url")
        val mergesUrl: String? = null,
        @Optional
        @SerialName("milestones_url")
        val milestonesUrl: String? = null,
        @Optional
        @SerialName("mirror_url")
        val mirrorUrl: String? = null,
        @Optional
        @SerialName("name")
        val name: String? = null,
        @Optional
        @SerialName("node_id")
        val nodeId: String? = null,
        @Optional
        @SerialName("notifications_url")
        val notificationsUrl: String? = null,
        @Optional
        @SerialName("open_issues")
        val openIssues: Int? = null,
        @Optional
        @SerialName("open_issues_count")
        val openIssuesCount: Int? = null,
        @Optional
        @SerialName("owner")
        val owner: Owner? = null,
        @Optional
        @SerialName("private")
        val `private`: Boolean? = null,
        @Optional
        @SerialName("pulls_url")
        val pullsUrl: String? = null,
        @Optional
        @SerialName("pushed_at")
        val pushedAt: String? = null,
        @Optional
        @SerialName("releases_url")
        val releasesUrl: String? = null,
        @Optional
        @SerialName("score")
        val score: Double? = null,
        @Optional
        @SerialName("size")
        val size: Int? = null,
        @Optional
        @SerialName("ssh_url")
        val sshUrl: String? = null,
        @Optional
        @SerialName("stargazers_count")
        val stargazersCount: Int? = null,
        @Optional
        @SerialName("stargazers_url")
        val stargazersUrl: String? = null,
        @Optional
        @SerialName("statuses_url")
        val statusesUrl: String? = null,
        @Optional
        @SerialName("subscribers_url")
        val subscribersUrl: String? = null,
        @Optional
        @SerialName("subscription_url")
        val subscriptionUrl: String? = null,
        @Optional
        @SerialName("svn_url")
        val svnUrl: String? = null,
        @Optional
        @SerialName("tags_url")
        val tagsUrl: String? = null,
        @Optional
        @SerialName("teams_url")
        val teamsUrl: String? = null,
        @Optional
        @SerialName("trees_url")
        val treesUrl: String? = null,
        @Optional
        @SerialName("updated_at")
        val updatedAt: String? = null,
        @Optional
        @SerialName("url")
        val url: String? = null,
        @Optional
        @SerialName("watchers")
        val watchers: Int? = null,
        @Optional
        @SerialName("watchers_count")
        val watchersCount: Int? = null
    ) {
        @Serializable
        data class Owner(
            @Optional
            @SerialName("avatar_url")
            val avatarUrl: String? = null,
            @Optional
            @SerialName("events_url")
            val eventsUrl: String? = null,
            @Optional
            @SerialName("followers_url")
            val followersUrl: String? = null,
            @Optional
            @SerialName("following_url")
            val followingUrl: String? = null,
            @Optional
            @SerialName("gists_url")
            val gistsUrl: String? = null,
            @Optional
            @SerialName("gravatar_id")
            val gravatarId: String? = null,
            @Optional
            @SerialName("html_url")
            val htmlUrl: String? = null,
            @Optional
            @SerialName("id")
            val id: Int? = null,
            @Optional
            @SerialName("login")
            val login: String? = null,
            @Optional
            @SerialName("node_id")
            val nodeId: String? = null,
            @Optional
            @SerialName("organizations_url")
            val organizationsUrl: String? = null,
            @Optional
            @SerialName("received_events_url")
            val receivedEventsUrl: String? = null,
            @Optional
            @SerialName("repos_url")
            val reposUrl: String? = null,
            @Optional
            @SerialName("site_admin")
            val siteAdmin: Boolean? = null,
            @Optional
            @SerialName("starred_url")
            val starredUrl: String? = null,
            @Optional
            @SerialName("subscriptions_url")
            val subscriptionsUrl: String? = null,
            @Optional
            @SerialName("type")
            val type: String? = null,
            @Optional
            @SerialName("url")
            val url: String? = null
        )

        @Serializable
        data class License(
            @Optional
            @SerialName("key")
            val key: String? = null,
            @Optional
            @SerialName("name")
            val name: String? = null,
            @Optional
            @SerialName("node_id")
            val nodeId: String? = null,
            @Optional
            @SerialName("spdx_id")
            val spdxId: String? = null,
            @Optional
            @SerialName("url")
            val url: String? = null
        )
    }
}