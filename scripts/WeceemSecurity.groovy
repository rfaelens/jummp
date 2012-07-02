// The policy closure must be assigned to the policy variable
policy = {

  // Here we define a role - this can be anything your authentication
  // system provides, but with Weceem Application edition, ROLE_GUEST, ROLE_USER,
  // and ROLE_ADMIN are used.
  // Note also that Weceem automatically adds the user's login as a special role
  // eg. user "fred" automatically has a role added called "USER_fred" for easy per-user
  // access control
  'ROLE_ADMIN' {
    // We're defining permissions for any space so use '*'. Alternatively
    // specify a list of space alias URIs eg: space 'internal', 'extranet' (no square brackets!)
    space '*'

    // Control whether this role can access Weceem admin functions eg edit/create spaces
    admin true

    // Control whether this role can create new content in this space
    create true

    // Control whether this role can edit content in this space
    edit true

    // Control whether this role can view content in this space
    view true

    // Control whether this role can delete content in this space
    delete true
  }

  'ROLE_USER' {
    space '*'
    admin false
    create false
    edit false
    view true
    delete false
  }

  'ROLE_GUEST' {
    space '*'
    admin false
    create false
    edit false
    view true
    delete false
  }
}
