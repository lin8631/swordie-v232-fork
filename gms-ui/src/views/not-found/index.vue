<template>
  <a-result
    class="not-found"
    status="404"
    :subtitle="'not found'"
  />
</template>

<script lang="ts" setup></script>

<style scoped lang="less">
  .not-found {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
</style>
