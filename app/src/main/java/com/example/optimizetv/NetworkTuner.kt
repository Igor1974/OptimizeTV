package com.example.optimizetv

import com.example.optimizetv.utils.RootUtils

object NetworkTuner {

    // Установка Google DNS
    fun setGoogleDNS(): String {
        return RootUtils.executeCommand(
            "setprop net.dns1 8.8.8.8"
        )
    }

    // Установка Cloudflare DNS
    fun setCloudflareDNS(): String {
        return RootUtils.executeCommand(
            "setprop net.dns1 1.1.1.1"
        )
    }

    // Установка OpenDNS
    fun setOpenDNS(): String {
        return RootUtils.executeCommand(
            "setprop net.dns1 208.67.222.222"
        )
    }

    // Включение TCP Fast Open
    fun enableTcpFastOpen(): String {
        return RootUtils.executeCommand(
            "echo 3 > /proc/sys/net/ipv4/tcp_fastopen"
        )
    }

    // Снижение задержек TCP
    fun reduceTcpLatency(): String {
        return RootUtils.executeCommand(
            "echo 1 > /proc/sys/net/ipv4/tcp_low_latency"
        )
    }

    // Очистка ARP кэша
    fun clearArpCache(): String {
        return RootUtils.executeCommand("arp -d -a")
    }

    // Установка MTU (размер пакета)
    fun setMTU(mtu: Int = 1500): String {
        return RootUtils.executeCommand("ip link set wlan0 mtu $mtu")
    }

    // Полная сетевая оптимизация
    fun fullNetworkOptimization(): String {
        val commands = """
            # DNS
            setprop net.dns1 8.8.8.8
            setprop net.dns2 8.8.4.4
            
            # TCP Optimizations
            echo 3 > /proc/sys/net/ipv4/tcp_fastopen
            echo 1 > /proc/sys/net/ipv4/tcp_tw_reuse
            echo 1 > /proc/sys/net/ipv4/tcp_low_latency
        """.trimIndent()

        return RootUtils.executeCommand(commands)
    }

    // Задать свой DNS-сервер
    fun setCustomDNS(dns: String): String {
        return RootUtils.executeCommand("setprop net.dns1 $dns")
    }

    // Отключить ненужные сетевые службы
    fun disableUnnecessaryNetworkServices() {
        // Пример: отключение неиспользуемых демонов
        val commands = """
            stop dnsmasq
            stop tether_dnsproxy_hw
        """

        RootUtils.executeCommand(commands)
    }
}