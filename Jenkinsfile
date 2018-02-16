node("launchpad-maven") {
  checkout scm
  stage("Test") {
    sh "mvn test"
  }
  stage("Deploy database") {
    sh "if ! oc get service database | grep database; then oc new-app -eINFLUXDB_ADMIN_USER=luke -eINFLUXDB_ADMIN_PASSWORD=secret -eINFLUXDB_DB=metric influxdb --name=database; fi"
  }
  stage("Deploy") {
    sh "mvn fabric8:deploy -Popenshift -DskipTests"
  }
}
